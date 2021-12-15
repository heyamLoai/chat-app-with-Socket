package com.example.networkprojectfinal

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ListView
import com.example.networkprojectfinal.Adapter.MessageAdapter
import com.example.networkprojectfinal.model.MessageFormat
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.Socket
import kotlinx.android.synthetic.main.activity_message_user.*
import org.json.JSONException
import org.json.JSONObject

class MessageUserActivity : AppCompatActivity() {

    lateinit var app: SocketCreate
    private var mSocket: Socket? = null
    var sourceId:String?=null
    var desId:String?=null
    var desName:String?=null
    var sourceName:String?=null
    private var hasConnection = false
    private var messageListView: ListView? = null
    private var messageAdapter: MessageAdapter? = null

    //ما اخدتو
    private var thread2: Thread? = null
    private var startTyping = false
    private var time = 2

    @SuppressLint("HandlerLeak")
    var handler2: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            Log.i("TAG", "handleMessage: typing stopped $startTyping")
            if (time == 0) {
                typing.text=""
                Log.i("TAG", "handleMessage: typing stopped time is $time")
                startTyping = false
                time = 2
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_user)

        app = application as SocketCreate
        mSocket = app.getSocket()
        desName=intent.getStringExtra("desName")
        desId = intent.getStringExtra("desId")
        sourceId=intent.getStringExtra("sourceId")
        sourceName=intent.getStringExtra("sourceName")
        var desNameFirstCapital = desName!![0].toUpperCase()+desName!!.substring(1)
        txtReceiverName.text= "$desNameFirstCapital's chat"



        mSocket!!.on(Socket.EVENT_CONNECT_ERROR) {
            runOnUiThread {
                Log.e("EVENT_CONNECT_ERROR", "EVENT_CONNECT_ERROR: ")
            }
        }
        mSocket!!.on(Socket.EVENT_CONNECT_TIMEOUT,  Emitter.Listener {
            runOnUiThread {
                Log.e("EVENT_CONNECT_TIMEOUT", "EVENT_CONNECT_TIMEOUT: ")

            }
        })
        mSocket!!.on(Socket.EVENT_CONNECT) {
            Log.e("onConnect", "Socket Connected!") }
        mSocket!!.on(Socket.EVENT_DISCONNECT, Emitter.Listener {
            runOnUiThread {
                Log.e("onDisconnect", "Socket onDisconnect!")

            }
        })

        sendButton.setOnClickListener {
            sendMessage()
        }

        if (savedInstanceState != null) {
            hasConnection = savedInstanceState.getBoolean("hasConnection")
        }
        if (hasConnection) {
        } else {
            mSocket!!.connect()
            mSocket!!.on("message", onNewMessage)
            mSocket!!.on("on typing", onTyping)
        }

        hasConnection = true

        messageListView = findViewById(R.id.rv_message)
        val messageFormatList: List<MessageFormat?> = java.util.ArrayList()
        messageAdapter = MessageAdapter(this, R.layout.item_message, messageFormatList)
        messageListView!!.adapter = messageAdapter
        onTypeButtonEnable()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("hasConnection", hasConnection)
    }

    private var onNewMessage = Emitter.Listener { args ->
        runOnUiThread {
            try {
                val data = args[0] as JSONObject
                if (sourceId.equals(data.getString("desId"))){
                    val message=data.getString("message")
                    val senderId = data.getString("sourceId")
                    val format = MessageFormat(senderId, desName!!, message)
                    messageAdapter!!.add(format)
                }else if(sourceId.equals(data.getString("sourceId"))){
                    val message=data.getString("message")
                    val senderId = data.getString("sourceId")
                    val format = MessageFormat(senderId, desName!!, message)
                    messageAdapter!!.add(format)
                }

            }catch (e:Exception){
                Log.e("TAG",e.toString())
            }
        }
    }


    private fun sendMessage() {
        var message = JSONObject()
        message.put("message", textField.text.toString())
        message.put("desId",desId)
        message.put("sourceId",sourceId)
        mSocket!!.emit("message", message)
        textField.text.clear()
    }

    fun onTypeButtonEnable() {
        textField!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
                val onTyping = JSONObject()
                try {
                    onTyping.put("typing", true)
                    onTyping.put("username", sourceName)
                    onTyping.put("uniqueId", sourceId)
                    onTyping.put("desId", desId)
                    mSocket!!.emit("on typing", onTyping)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                sendButton!!.isEnabled = charSequence.toString().trim { it <= ' ' }.isNotEmpty()
            }

            override fun afterTextChanged(editable: Editable) {}
        })
    }



    var onTyping = Emitter.Listener { args ->

        runOnUiThread(Runnable {
            var data =  args[0] as JSONObject
            try {
                var typingOrNot = data.getBoolean("typing");
                var userName = data.getString("username") + " is Typing......";
                var id = data.getString("uniqueId")
                var destenationId=data.getString("desId")

                if(id == sourceId){
                    typingOrNot = false;
                }else {
                    if (destenationId == sourceId){
                        typing.text = userName
                    }
                }

                if(typingOrNot){
                    if(!startTyping){
                        startTyping = true;
                        thread2= Thread(
                            Runnable() {
                                while(time > 0) {
                                    synchronized (this){
                                        try {
                                            Thread.sleep(1000);
                                            Log.i("TAG", "run: typing " + time);
                                        } catch (e:InterruptedException) {
                                            e.printStackTrace();
                                        }
                                        time--
                                    }
                                    handler2.sendEmptyMessage(0);
                                }

                            })
                        thread2!!.start();

                    }else {
                        time = 2;
                    }

                }
            } catch (e:JSONException) {
                e.printStackTrace();
            }
        })
    }


}