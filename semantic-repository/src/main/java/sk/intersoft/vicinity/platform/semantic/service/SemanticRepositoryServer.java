package sk.intersoft.vicinity.platform.semantic.service;

public class SemanticRepositoryServer {
    public static void main(String [] args) throws Exception {
        SemanticRepositoryComponent component = new SemanticRepositoryComponent();
        component.start();

        System.out.println("starting");

    }
}
