package com.opex.ansibletoki.utils;

import java.io.File;

public class FileWalker {

    public void walk( String path, FileWalkHandler handler ){

        File root = new File( path );
        File[] list = root.listFiles();

        if (list == null) return;

        for ( File f : list ) {
        	handler.handle(f);
            if ( f.isDirectory() ) {
                walk( f.getAbsolutePath(), handler );
                
            }
            else {
             
            }
        }
    }

   
}