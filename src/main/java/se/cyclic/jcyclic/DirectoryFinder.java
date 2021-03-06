package se.cyclic.jcyclic;


import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * This class finder analyses a directory and all sub-directories. Any class files found will be analysed.
 * Inner classes and anonymous inner classes will be ignored.
 */
public class DirectoryFinder implements ClassFinder {
    private File directory;
    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryFinder.class);
    /**
     * @param directory
     */
    public DirectoryFinder(File directory) {
        this.directory = directory;
    }

    private List<JavaClass> getJavaClassList() {
        LOGGER.debug("Searching for class files in " + directory.getAbsolutePath());
        if (!directory.exists()) {
            return Collections.emptyList();
        }
        Collection<File> files = FileUtils.listFiles(directory, new SuffixFileFilter("class"), DirectoryFileFilter.DIRECTORY);
        
        try {
            List<JavaClass> javaClasses = new ArrayList<>();
            for (File file : files) {
                LOGGER.debug("Class file " + file.getName());
                String fullyQualifiedClassName = getClassNameFromFile(file);
                if (!fullyQualifiedClassName.contains("$")) {
                    ClassParser classParser = new ClassParser(file.getAbsolutePath());
                    javaClasses.add(classParser.parse());
                }
            }
            LOGGER.debug("Done searching for class files in " + directory.getAbsolutePath()+". Analysed "+javaClasses.size() + " classes");
            return javaClasses;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    @Override
    public Collection<JavaClassInformation> getJavaClassInformationList() {
        List<JavaClassInformation> classes = new ArrayList<>();
        for (JavaClass javaClass : getJavaClassList()) {
            classes.add(new JavaClassInformation(javaClass));
        }
        return classes;
    }


    private String getClassNameFromFile(File file) {
        String classNameWithSlashes = file.getAbsolutePath().replace(directory.getAbsolutePath(), "");
        String classNameWithDots = classNameWithSlashes.replace(File.separator, ".");
        if (classNameWithDots.substring(0, 1).equals(".")) {
            classNameWithDots = classNameWithDots.substring(1);
        }
        if (classNameWithDots.endsWith(".class")) {
            classNameWithDots = classNameWithDots.substring(0, classNameWithDots.length() - 6);
        }
        return classNameWithDots;
    }
}
