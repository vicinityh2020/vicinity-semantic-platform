package sk.intersoft.vicinity.platform.semantic.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SemanticRepositoryServer {
    final static Logger logger = LoggerFactory.getLogger(SemanticRepositoryServer.class.getName());

    public static void main(String [] args) throws Exception {

        SemanticRepositoryComponent component = new SemanticRepositoryComponent();
        component.start();

        logger.info("starting semantic repository server");

    }
}
