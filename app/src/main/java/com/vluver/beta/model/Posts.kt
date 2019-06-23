package com.vluver.beta.model


import java.io.Serializable
import java.util.ArrayList

/**
 * Created by JUNED on 6/16/2016.
 */
data class Posts (

    var _post_id: String? = null,
    var user: String? = null,
    var description: String? = null,
    var date: String? = null,
    var avatar: String? = null,
    var _num_likes: Int = 0,
    var _our_like: Boolean = false,

    var num_imgs: Int = 0,
    //public Posts(){}
    /*public Posts(String user,String image,String description,Date date,String avatar){
        this.user =user;
        this.image = image;
        this.description = description;
        this.date = date;
        this.avatar = avatar;
    }*/

    var pathimg: ArrayList<String>? = null,
    var nameimg: ArrayList<String>? = null


):Serializable
