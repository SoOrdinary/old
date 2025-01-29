package com.soordinary.old.view

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.soordinary.old.OldApplication
import com.soordinary.old.R
import com.soordinary.old.databinding.ItemContactPagerBinding
import com.soordinary.old.databinding.ItemContactRecycleBinding
import com.soordinary.old.room.entity.Contact

/**
 * 列表适配器，类型0则为ViewPager，1为RecycleView
 */
class ContactAdapter(private val mainActivity: MainActivity,private val contactList: List<Contact>, private val itemType: Int): RecyclerView.Adapter<ContactAdapter.BaseViewHolder>() {

    val listener = mainActivity.OnClickItemListener()

    // 内部基类，简化多种适配item与bind的书写
    abstract inner class BaseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(contact: Contact)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        when(viewType){
            0-> {
                val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_contact_pager, parent, false)
                return PagerViewHolder(view)
            }
            1-> {
                val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_contact_recycle, parent, false)
                return RecycleViewHolder(view)
            }
            else-> {
                Toast.makeText(OldApplication.context, "传入布局ID错误", Toast.LENGTH_SHORT).show()
                val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_contact_pager, parent, false)
                return PagerViewHolder(view)
            }
        }
    }


    // 三个重写函数，依次说明布局类型、item个数、声明绑定(调用对应布局Holder重写的binding函数)
    override fun getItemViewType(position: Int) =  itemType
    override fun getItemCount() = contactList.size
    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) = holder.bind(contactList[position])

    inner class PagerViewHolder(view: View) : BaseViewHolder(view){
        private val binding = ItemContactPagerBinding.bind(view)

        override fun bind(contact: Contact) {
            binding.contactName.text =contact.name
            binding.callButton.setOnClickListener {
               listener.onClickPhone(contact)
            }
            binding.wechatButton.setOnClickListener {
                listener.onClickWechat(contact)
            }
        }
    }

    inner class RecycleViewHolder(view: View) : BaseViewHolder(view){
        private val binding = ItemContactRecycleBinding.bind(view)

        override fun bind(contact: Contact) {
            binding.root.setOnLongClickListener(null)
            binding.name.text = contact.name
            binding.phone.text = contact.phone
            binding.root.setOnClickListener {
                listener.onClickRecycleItem(adapterPosition)
            }
            binding.root.setOnLongClickListener {
                listener.onLongClickRecycleItem(contact)
                true
            }
        }
    }
}