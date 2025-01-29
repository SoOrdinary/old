package com.soordinary.old.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * 联系人的存储实体类
 */
@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,            // 自增主键
    var name: String,           // 联系人姓名
    var phone: String,          // 电话号码
    var wxId: String? = null    // 微信跳转url
)