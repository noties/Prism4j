package io.noties.prism4j.bundler;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

abstract class ListResources {

    @NotNull
    abstract List<String> listResourceFiles(@NotNull Class<?> type, @NotNull String folder);

    @NotNull
    static ListResources create() {
        return new Impl();
    }

    static class Impl extends ListResources {

        // thanks to http://www.uofr.net/~greg/java/get-resource-listing.html
        @NotNull
        @Override
        List<String> listResourceFiles(@NotNull Class<?> type, @NotNull String folder) {

            URL url = type.getClassLoader().getResource(folder);

            if (url != null
                    && "file".equals(url.getProtocol())) {
                try {
                    final File file = new File(url.toURI());
                    final String[] files = file.list();
                    if (files != null
                            && files.length > 0) {
                        final List<String> list = new ArrayList<>(files.length);
                        Collections.addAll(list, files);
                        return list;
                    } else {
                        throw new RuntimeException("Unexpected state, no files found for resource folder: " + folder);
                    }
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }

            if (url == null) {
                final String me = type.getName().replace('.', '/') + ".class";
                url = type.getClassLoader().getResource(me);
                if (url == null) {
                    throw new RuntimeException("Unexpected state, cannot obtain a resource");
                }
            }

            if (!"jar".equals(url.getProtocol())) {
                throw new RuntimeException("Cannot obtain resource for url (expecting `jar` " +
                        "protocol): " + url);
            }

            final String path = url.getPath();
            final String jarPath = path.substring(5, path.indexOf('!'));
            try {
                final JarFile jarFile = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
                final Enumeration<JarEntry> jarEntries = jarFile.entries();
                final List<String> strings = new ArrayList<>();
                final int start = folder.length();
                String name;
                int index;
                while (jarEntries.hasMoreElements()) {
                    name = jarEntries.nextElement().getName();
                    if (name.startsWith(folder)) {
                        name = name.substring(start);
                        index = name.indexOf('/');
                        if (index > -1) {
                            name = name.substring(0, index);
                        }
                        strings.add(name);
                    }
                }
                return strings;
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }
}
