/*
 * (C) Copyright 2006-2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     bstefanescu
 */
package org.nuxeo.dev;

import java.io.File;
import java.net.URL;

/**
 * This is a sample of how to use NuxeoApp.
 * This sample is building a core server version 5.3.1-SNAPSHOT,
 * and then starts it.
 * 
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class Main {

    public static void main(String[] args) throws Exception {
        File home = null;
        String version = "5.3.1-SNAPSHOT";
        String profile = NuxeoApp.CORE_SERVER;
        String host = "localhost";
        int port = 8080;
        String config = null;
        String opt = null;
        for (String arg : args) {
            if (arg.startsWith("-")) {
                opt = arg;
            } else if (opt != null) {
                if ("-v".equals(opt)) {
                    version = arg;
                } else if ("-p".equals(opt)) {
                    profile = arg;
                } else if ("-h".equals(opt)) {
                    int p = arg.indexOf(':');
                    if (p != -1) {
                        host = arg.substring(0, p);
                        port = Integer.parseInt(arg.substring(p+1));
                    } else {
                        host = arg;
                    }
                } else if ("-c".equals(opt)) {
                    config = arg;
                }
                opt = null;
            } else { // the home directory
                home = arg.startsWith("/") ? new File(arg) : new File(".", arg);
                opt = null;
            }
        }
        
        if (home == null) {
            System.err.println("Syntax error: You must specify a home directory to be used by the nuxeo server.");
            System.exit(1);
        }

        home = home.getCanonicalFile();
        
        System.out.println("+---------------------------------------------------------");
        System.out.println("| Nuxeo Server Profile: "+(profile==null?"custom":profile)+"; version: "+version);
        System.out.println("| Home Directory: "+home);
        System.out.println("| HTTP server at: "+host+":"+port);
        System.out.println("+---------------------------------------------------------\n");
        
        
        //FileUtils.deleteTree(home);
        final NuxeoApp app = new NuxeoApp(home);
        if (config != null) {
            app.build(makeUrl(config), version, true);
        } else {
            app.build(profile, version, true);
        }
        NuxeoApp.setHttpServerAddress(host, port);
        Runtime.getRuntime().addShutdownHook(new Thread("Nuxeo Server Shutdown") {
            @Override
            public void run() {
                try {
                    app.shutdown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        
        app.start();
        
        //System.out.println("Hello!!");
        //app.shutdown();
    }

    
    protected static URL makeUrl(String spec) {
        try {
        if (spec.indexOf(':') > -1) {
            if (spec.startsWith("java:")) {
                spec = spec.substring(5);
                ClassLoader cl = getContextClassLoader();
                URL url = cl.getResource(spec);
                if (url == null) {
                    fail("Canot found java resource: "+spec);
                }
                return url;
            } else {
                return new URL(spec);
            }
        } else {
            return new File(spec).toURI().toURL();
        }
        } catch (Exception e) {
            fail("Invalid config file soecification. Not a valid URL or file: "+spec);
            return null;
        }
    }
    
    protected static void fail(String msg) {
        System.err.println(msg);
        System.exit(2);
    }
    
    protected static ClassLoader getContextClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return cl == null ? Main.class.getClassLoader() : cl;
    }

}
