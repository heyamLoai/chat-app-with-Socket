package com.example.networkprojectfinal.Adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.networkprojectfinal.R
import com.example.networkprojectfinal.modle.addGroupModel
import kotlinx.android.synthetic.main.add_group_item.view.*
import kotlinx.android.synthetic.main.user_item.view.*

class addGroupAdpter(var activity: Context, var data: ArrayList<addGroupModel>, var clickListener: onUserItemClickListener) :
    RecyclerView.Adapter<addGroupAdpter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val user_image=itemView.avatar_add_group
        val user_name=itemView.name_user_add_group
        val user_Cheack=itemView.rdb_choose_user


        fun initialize(data: addGroupModel, action:onUserItemClickListener){
            user_image.setImageResource(R.drawable.logo)
            user_name.text=data.name
            user_Cheack.isChecked=data.isCheak!!


            itemView.setOnClickListener {
                action.onItemClick(data,adapterPosition)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(activity).inflate(R.layout.add_group_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.initialize(data[position], clickListener)
    }

    interface onUserItemClickListener{
        fun onItemClick(data:addGroupModel, position: Int)
    }
}
