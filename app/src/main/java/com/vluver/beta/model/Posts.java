package com.vluver.beta.model;

import java.util.ArrayList;

/**
 * Created by JUNED on 6/16/2016.
 */
public class Posts {

    private String post_id;
    private String user;
    private String description;
    private String date;
    private String avatar;
    private int num_likes;
    private boolean our_like;
    private int num_imgs;
    private ArrayList<String> pathimg;
    private ArrayList<String> nameimg;


    //public Posts(){}
    /*public Posts(String user,String image,String description,Date date,String avatar){
        this.user =user;
        this.image = image;
        this.description = description;
        this.date = date;
        this.avatar = avatar;
    }*/

    public ArrayList<String> getPathimg() {
        return pathimg;
    }

    public void setPathimg(ArrayList<String> pathimg) {
        this.pathimg = pathimg;
    }

    public ArrayList<String> getNameimg() {
        return nameimg;
    }

    public void setNameimg(ArrayList<String> nameimg) {
        this.nameimg = nameimg;
    }

    public void set_our_like (Boolean our_like) {
        this.our_like = our_like;
    }

    public boolean get_our_like() {

        return our_like;
    }

    public void set_num_likes (int num_likes) {
        this.num_likes = num_likes;
    }

    public int get_num_likes() {

        return num_likes;
    }


    public void set_post_id(String post_id) {
        this.post_id = post_id;
    }

    public String get_post_id() {

        return post_id;
    }
    public void setUser(String user) {
        this.user = user;
    }

    public String getUser() {

        return user;
    }



    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {

        return description;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getAvatar() {
        return avatar;
    }

    public int getNum_imgs() {
        return num_imgs;
    }

    public void setNum_imgs(int num_imgs) {
        this.num_imgs = num_imgs;
    }
}