package org.python.antlr;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.tree.CommonErrorNode;

/** A node representing erroneous token range in token stream
 */
public class PythonErrorNode extends PythonTree {

    private CommonErrorNode errorNode;

    public PythonErrorNode(TokenStream input, Token start, Token stop,
                           RecognitionException e) {
        this.errorNode = new CommonErrorNode(input, start, stop, e);
    }

    public PythonErrorNode(CommonErrorNode errorNode){
        this.errorNode = errorNode;
    }

    @Override
    public boolean isNil() {
        return errorNode.isNil();
    }

    @Override
    public int getAntlrType() {
        return errorNode.getType();
    }

    @Override
    public String getText() {
        return errorNode.getText();
    }

    @Override
    public String toString() {
        return errorNode.toString();
    }
}
