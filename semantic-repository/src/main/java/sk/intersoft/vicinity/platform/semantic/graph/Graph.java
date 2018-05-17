package sk.intersoft.vicinity.platform.semantic.graph;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.json.JSONObject;
import sk.intersoft.vicinity.platform.semantic.ontology.Namespaces;

import java.util.HashSet;
import java.util.Iterator;

public class Graph {
    public String baseURI = "";
    private TreeModel model = new TreeModel();
    ValueFactory factory = SimpleValueFactory.getInstance();

    public Graph(String baseURI){
        this.baseURI = baseURI;
    }

    public void add(Statement st) {
        model.add(st);
    }


    public HashSet<String> values(String property) {

        HashSet<String> values = new HashSet<String>();
        Model m = model.filter(factory.createIRI(baseURI), factory.createIRI(Namespaces.toURI(property)), null);

        Iterator<Statement> i = m.iterator();
        while(i.hasNext()){
            values.add(i.next().getObject().stringValue());
        }
        return values;
    }

    public String value(String property) {
        HashSet<String> values = values(property);
        if(values.size() == 0){
            return null;
        }
        return values.iterator().next();
    }


    private Graph subGraph(Value base, Graph g){
        if(base instanceof Resource){
            Iterator<Statement> i = model.filter((Resource)base, null, null).iterator();
            while(i.hasNext()){
                Statement s = i.next();
                g.add(s);
                subGraph(s.getObject(), g);
            }
        }
        return g;
    }

    private Graph subGraph(Statement s){
        Value base = s.getObject();
        Graph g = new Graph(base.stringValue());
        g.add(s);
        return subGraph(base, g);
    }

    public HashSet<Graph> subGraphs(String property) {
        HashSet<Graph> graphs = new HashSet<Graph>();
        Iterator<Statement> i = model.filter(factory.createIRI(baseURI), factory.createIRI(Namespaces.toURI(property)), null).iterator();
        while(i.hasNext()){
            Statement s = i.next();
            graphs.add(subGraph(s));
        }
        return graphs;
    }
    public Graph subGraph(String property) {
        HashSet<Graph> graphs = subGraphs(property);
        if(graphs.size() == 0){
            return null;
        }
        return graphs.iterator().next();
    }


    private String indent(int indent, String cnt) {
        String out = "";
        for(int i = 0; i < indent; i++){
            out += "  ";
        }
        return out + "> " + cnt + "\n";
    }

    private String describe(Value base, int indent) {
//        System.out.println("DESCRIBE ["+baseURI+"]: "+base);
//        System.out.println("model dump: "+model.size());
//        Iterator<Statement> mi = model.iterator();
//        while(mi.hasNext()){
//            Statement st = mi.next();
//            System.out.println(
////                        Namespaces.toPrefixed(st.getSubject().stringValue()) + " " +
////                                Namespaces.toPrefixed(st.getPredicate().stringValue()) + " "+
////                                Namespaces.toPrefixed(st.getObject().stringValue()));
//            st.getSubject().stringValue() + " " +
//                    st.getPredicate().stringValue() + " "+
//                    st.getObject().stringValue());
//
//        }

        String out = "";
        if(base instanceof Resource){
            IRI baseURI = factory.createIRI(base.stringValue());
            out += indent(indent, Namespaces.toPrefixed(baseURI.stringValue()));
            indent += 2;
            Iterator<Statement> i = model.filter((Resource)base, null, null).iterator();
            while(i.hasNext()){
                Statement s = i.next();
                System.out.println(s);
                Value obj = s.getObject();
                String value = obj.stringValue();
                if(obj instanceof Resource) {
                    IRI objURI = factory.createIRI(obj.stringValue());
                    value = Namespaces.toPrefixed(value);
                }
                if(obj instanceof Literal){
                    Literal literal = (Literal)obj;
                    IRI dt = literal.getDatatype();
                    value = "\"" +
                            literal.stringValue() +
                            "\" type of " +
                            Namespaces.toPrefixed(dt.stringValue());
                }
                IRI p = s.getPredicate();

                out += indent(indent, Namespaces.toPrefixed(p.stringValue()) + ": " + value);
                if(!p.stringValue().equals(Namespaces.rdf)){
                    out += describe(s.getObject(), (indent + 2));
                }
            }
        }
        return out;
    }
    public String describe() {
        return describe(factory.createIRI(baseURI), 0);
    }

    private String describe(int indent) {

        return describe(factory.createIRI(baseURI), indent);
    }



    @Override
    public String toString(){
        return "[GRAPH: " + this.baseURI + ":" + model.size() + "]";
    }


    public String show(){
        String out = "[GRAPH: " + this.baseURI + ":" + model.size() + "]\n";
        Iterator<Statement> i = model.iterator();
        while(i.hasNext()){
            Statement s = i.next();
            out += "  ["+s.getSubject()+":"+s.getPredicate()+":"+s.getObject()+":"+s.getContext()+"]\n";
        }
        return out;
    }

}
