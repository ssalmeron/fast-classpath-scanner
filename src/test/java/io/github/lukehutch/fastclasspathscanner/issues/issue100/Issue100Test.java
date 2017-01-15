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
 * Copyright (c) 2016 Luke Hutchison
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
package io.github.lukehutch.fastclasspathscanner.issues.issue100;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import org.junit.Test;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;

public class Issue100Test {
    @Test
    public void issue100Test() {
        final ClassLoader classLoader = Issue100Test.class.getClassLoader();
        final URL aJarURL = classLoader.getResource("issue100-has-field-a.jar");
        final URL bJarURL = classLoader.getResource("issue100-has-field-b.jar");
        final URLClassLoader overrideClassLoader = new URLClassLoader(new URL[] { aJarURL, bJarURL });

        // Class issue100.Test with field "a" should mask class of same name with field "b",
        // because "...a.jar" is earlier in classpath than "...b.jar"
        final ArrayList<String> fieldNames1 = new ArrayList<>();
        new FastClasspathScanner() //
                .overrideClassLoaders(overrideClassLoader).matchAllClasses(klass -> {
                    for (final Field f : klass.getFields()) {
                        fieldNames1.add(f.getName());
                    }
                }).scan();
        assertThat(fieldNames1).containsOnly("a");

        // However, if "...b.jar" is specifically whitelisted, the classloader for "...a.jar" should not 
        // be used to load the class, it should be skipped
        final ArrayList<String> fieldNames2 = new ArrayList<>();
        new FastClasspathScanner("jar:issue100-has-field-b.jar") //
                .overrideClassLoaders(overrideClassLoader).matchAllClasses(klass -> {
                    for (final Field f : klass.getFields()) {
                        fieldNames2.add(f.getName());
                    }
                }).scan();
        assertThat(fieldNames2).containsOnly("b");
    }
}
