/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package eu.hydrologis.stage.modules.core;

public enum LogStyle {
    NORMAL("",""),
    COMMENT("<span style='font:bold 16px Arial; color:#333333'>","</span>"),
    ERROR("<span style='font:bold 16px Arial; color:#FF0000'>", "</span>");
    
    private String pre;
    private String post;

    LogStyle(String pre, String post){
        this.pre = pre;
        this.post = post;
    }
    
    
    public String getPre() {
        return pre;
    }
    
    public String getPost() {
        return post;
    }
}
