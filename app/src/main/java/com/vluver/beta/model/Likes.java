package com.vluver.beta.model;


/**
 * Created by JUNED on 6/16/2016.
 */
public class Likes{

    private String user_id;
    private String post_id;

    public Likes() {

    }

    public Likes(String user_id, String post_id){
        this.user_id =user_id;
        this.post_id = post_id;

    }


    public void set_user_id(String user) {
        this.user_id = user_id;
    }

    public String get_user_id() {

        return user_id;
    }


    public void set_post_id(String post_id) {
        this.post_id = post_id;
    }

    public String get_post_id() {

        return post_id;
    }
}
