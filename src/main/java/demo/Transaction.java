package demo;

import org.w3c.dom.*;

public interface Transaction {
    void parse(Element element);

    /**
     * TODO: Perhaps return the execution result
     */
    void execute();
}
