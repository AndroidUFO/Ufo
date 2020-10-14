package com.androidufo.ufo.api.compiler.processsor;

import javax.annotation.processing.*;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.Set;

public abstract class AbsProcessor extends AbstractProcessor {

    public static final boolean DEBUG = true;

    protected Elements elementUtils;
    protected Types typeUtils;
    protected Messager messager;
    protected Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment pEnv) {
        super.init(pEnv);
        elementUtils = pEnv.getElementUtils();
        typeUtils = pEnv.getTypeUtils();
        messager = pEnv.getMessager();
        filer = pEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set.isEmpty()) {
            return false;
        }
        handleAnnotations(roundEnvironment);
        return true;
    }

    protected void printMsg(String msg) {
        if (DEBUG) {
            messager.printMessage(Diagnostic.Kind.NOTE, msg);
        }
    }

    protected void printError(String msg) {
        messager.printMessage(Diagnostic.Kind.ERROR, msg);
    }

    public abstract void handleAnnotations(RoundEnvironment roundEnvironment);

}
