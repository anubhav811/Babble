package com.anubhav.chatapp.models

import java.io.Serializable

data class UserStatus(
    var name: String,
    var profileImg: String,
    var lastUpdated : Long,
    var statuses : ArrayList<Status>
):Serializable
{
    constructor():this("","",0L, ArrayList())
    constructor(name: String, profileImg: String, lastUpdated: Long):this(name,profileImg,lastUpdated,ArrayList<Status>())
}
