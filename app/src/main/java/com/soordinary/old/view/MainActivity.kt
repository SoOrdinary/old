package com.soordinary.old.view

import android.Manifest
import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.soordinary.old.OldApplication
import com.soordinary.old.base.BaseActivity
import com.soordinary.old.databinding.ActivityMainBinding
import com.soordinary.old.databinding.DialogAddBinding
import com.soordinary.old.databinding.DialogListBinding
import com.soordinary.old.room.entity.Contact
import com.soordinary.old.utils.PermissionUtils
import java.util.Locale

class MainActivity : BaseActivity<ActivityMainBinding>(), TextToSpeech.OnInitListener {

    companion object {
        // 静态打开方法，指明打开该类需要哪些参数
        fun actionStart(context: Context) {
            val intent = Intent(context, MainActivity::class.java).apply {
                // putExtra()
            }
            context.startActivity(intent)
        }
    }

    private lateinit var launcher: ActivityResultLauncher<String>
    private lateinit var handleWay:(Boolean)->Unit
    private val viewModel :MainViewModel by viewModels()
    // 创建dialog实例，关于RecycleView
    private lateinit var dialog :Dialog
    private lateinit var recycleView:RecyclerView
    private lateinit var textToSpeech: TextToSpeech
    private var isFirstPageSelected = true // 标志位，用于判断是否是首次页面选中

    override fun getBindingInflate() = ActivityMainBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化 TextToSpeech
        textToSpeech = TextToSpeech(this, this)

        // 初始化一个提醒函数体，避免后续未初始化直接调用
        handleWay= {Toast.makeText(this,"处理事件未初始化",Toast.LENGTH_SHORT).show()}
        // 注册一个事件返回器
        launcher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            handleWay(isGranted)
        }


        with(DialogListBinding.inflate(layoutInflater)) {
            dialog = Dialog(this@MainActivity)
            dialog.setContentView(root)
            dialog.setCancelable(true)

            with(list){
                recycleView=this
                layoutManager = LinearLayoutManager(
                    this@MainActivity,
                    LinearLayoutManager.VERTICAL, false
                )
                adapter = ContactAdapter(this@MainActivity,viewModel.contactList, 1)
            }

            // 导入通讯录,长按才可以
            importContact.setOnLongClickListener {
                handleWay = { isGranted ->
                    if (isGranted) {
                        importContacts()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "读取通讯录权限被拒绝",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                PermissionUtils.requestPermission(
                    this@MainActivity,
                    launcher,
                    Manifest.permission.READ_CONTACTS
                ){
                    importContacts()
                }
                true
            }
        }

        with(binding.contactPager) {
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            adapter = ContactAdapter(this@MainActivity,viewModel.contactList, 0)

            // 监听 ViewPager2 页面滑动事件
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (!isFirstPageSelected && position < viewModel.contactList.size) {
                        val contact = viewModel.contactList[position]
                        speakName(contact.name)
                    }
                    isFirstPageSelected = false // 首次页面选中后，将标志位设为 false
                }
            })
        }

        viewModel.allContactsLiveData.observe(this){
            viewModel.contactList.clear()
            viewModel.contactList.addAll(it)
            binding.contactPager.adapter?.notifyDataSetChanged()
            recycleView.adapter?.notifyDataSetChanged()
        }

        binding.contactAdd.setOnClickListener {
            with(DialogAddBinding.inflate(layoutInflater)){
                val dialog= Dialog(this@MainActivity)
                dialog.setContentView(root)
                dialog.setCancelable(true)

                confirm.setOnClickListener {
                    if(name.text.isNullOrEmpty()||phone.text.isNullOrEmpty()){
                        Toast.makeText(this@MainActivity,"联系人信息不可为空",Toast.LENGTH_SHORT).show()
                    }else{
                        val num = phone.text.toString().trim()
                        val formattedPhone = if (num.length == 11) {
                            "${num.substring(0, 3)} ${num.substring(3, 7)} ${num.substring(7)}"
                        } else {
                            num
                        }
                        viewModel.insertContact(Contact(name=name.text.toString().trim(), phone = formattedPhone))
                        dialog.dismiss()
                        Toast.makeText(this@MainActivity,"添加成功",Toast.LENGTH_SHORT).show()
                    }
                }

                dialog.show()
            }
        }

        binding.contactList.setOnClickListener {
            dialog.show()
        }
    }

    private fun importContacts() {
        val contentResolver: ContentResolver = contentResolver
        val cursor: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val name = it.getString(nameIndex)
                val phone = it.getString(phoneIndex)
                val contact = Contact(name = name, phone = phone)
                viewModel.insertContact(contact)
            }
        }
        Toast.makeText(this, "通讯录导入成功", Toast.LENGTH_SHORT).show()
    }


    inner class OnClickItemListener(){

        // 点击拨打电话
        fun onClickPhone(contact: Contact){
            handleWay = {
                if(it){
                    callPhone(contact)
                }else{
                    Toast.makeText(this@MainActivity,"拨打权限被拒绝",Toast.LENGTH_SHORT).show()
                }
            }
            PermissionUtils.requestPermission(this@MainActivity,launcher,Manifest.permission.CALL_PHONE){
                callPhone(contact)
            }
        }

        private fun callPhone(contact: Contact){
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:${contact.phone}")
            startActivity(intent)
        }

        // 点击打开微信
        fun onClickWechat(contact: Contact){
            if (isWechatInstalled()) {
                openWechat()
            } else {
                Toast.makeText(this@MainActivity, "未安装微信应用", Toast.LENGTH_SHORT).show()
            }
        }

        private fun isWechatInstalled(): Boolean {
            val packageManager = packageManager
            return try {
                packageManager.getPackageInfo("com.tencent.mm", PackageManager.GET_ACTIVITIES)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }

        private fun openWechat() {
            val launchIntent = packageManager.getLaunchIntentForPackage("com.tencent.mm")
            if (launchIntent != null) {
                startActivity(launchIntent)
            } else {
                Toast.makeText(this@MainActivity, "无法打开微信应用", Toast.LENGTH_SHORT).show()
            }
        }

        // 单击调转到对应页
        fun onClickRecycleItem(position:Int){
            binding.contactPager.setCurrentItem(position,true)
            dialog.dismiss()
        }
        // 长按删除对应Item
        fun onLongClickRecycleItem(contact: Contact){
            viewModel.deleteContact(contact)
        }
    }

    private fun speakName(name: String) {
        if (textToSpeech.isSpeaking) {
            textToSpeech.stop()
        }
        textToSpeech.speak(name, TextToSpeech.QUEUE_FLUSH, null, null)
    }
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale.CHINA)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "语音播报语言不支持", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "语音播报初始化失败", Toast.LENGTH_SHORT).show()
        }
    }
}