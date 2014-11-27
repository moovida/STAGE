package eu.hydrologis.rap.stage.core;

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
