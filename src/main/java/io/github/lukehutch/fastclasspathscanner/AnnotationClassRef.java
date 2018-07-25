/*
 * This file is part of FastClasspathScanner.
 *
 * Author: Luke Hutchison
 *
 * Hosted at: https://github.com/lukehutch/fast-classpath-scanner
 *
 * --
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Luke Hutchison
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO
 * EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.lukehutch.fastclasspathscanner;

import java.util.Set;

import io.github.lukehutch.fastclasspathscanner.utils.Parser.ParseException;

/**
 * Stores a class descriptor in an annotation as a class type string, e.g. "[[[java/lang/String;" is stored as
 * "String[][][]".
 *
 * <p>
 * Use ReflectionUtils.typeStrToClass() to get a {@code Class<?>} reference from this class type string.
 */
public class AnnotationClassRef extends ScanResultObject {
    String typeDescriptorStr;
    transient TypeSignature typeSignature;
    private transient String className;

    AnnotationClassRef() {
    }

    AnnotationClassRef(final String typeDescriptorStr) {
        this.typeDescriptorStr = typeDescriptorStr;
    }

    @Override
    void setScanResult(final ScanResult scanResult) {
        super.setScanResult(scanResult);
        if (this.typeSignature != null) {
            this.typeSignature.setScanResult(scanResult);
        }
    }

    /**
     * Get the type signature for a type reference used in an annotation parameter.
     * 
     * @return The type signature of the annotation class ref.
     */
    private TypeSignature getTypeSignature() {
        if (typeSignature == null) {
            try {
                final TypeSignature typeSig = TypeSignature.parse(typeDescriptorStr, scanResult);
                typeSignature = typeSig;
            } catch (final ParseException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return typeSignature;
    }

    /** Return the name of the referenced class. */
    @Override
    public String getClassName() {
        if (className == null) {
            getTypeSignature();
            if (typeSignature instanceof BaseTypeSignature) {
                className = ((BaseTypeSignature) typeSignature).getBaseType().getName();
            } else if (typeSignature instanceof ClassRefTypeSignature) {
                className = ((ClassRefTypeSignature) typeSignature).getClassNameAndSuffixesWithoutTypeArguments();
            } else {
                throw new IllegalArgumentException("Got unexpected type " + typeSignature.getClass().getName()
                        + " for ref type signature: " + typeDescriptorStr);
            }
        }
        return className;
    }

    /** Return the class reference as a {@link ClassInfo} object. */
    @Override
    public ClassInfo getClassInfo() {
        getClassName();
        return super.getClassInfo();
    }

    @Override
    protected void getClassNamesFromTypeDescriptors(final Set<String> classNames) {
        classNames.add(getClassName());
    }

    @Override
    public int hashCode() {
        return getTypeSignature().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof AnnotationClassRef)) {
            return false;
        }
        return getTypeSignature().equals(((AnnotationClassRef) obj).getTypeSignature());
    }

    @Override
    public String toString() {
        return getTypeSignature().toString();
    }
}