package com.example.hiline.adapter.forum

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.hiline.service.PrefManager
import com.example.hiline.R
import com.example.hiline.service.Retro
import com.example.hiline.service.ForumService
import com.example.hiline.model.CommentModel
import com.example.hiline.response.CommentResponse
import com.example.hiline.response.FavResponse
import com.example.hiline.service.TokenAuthenticator
import com.example.hiline.user.forum.LaporkanKomentarActivity
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForumCommentUserAdapter(
    private var activity: Activity,
    private var context: Context,
    private var commentModels: ArrayList<CommentModel>
): RecyclerView.Adapter<ForumCommentUserAdapter.MyViewHolder>() {

    private var prefManager: PrefManager = PrefManager(context)

    inner class MyViewHolder(view: View): RecyclerView.ViewHolder(view){
        val cvPPForum: CardView = view.findViewById(R.id.cvPPForum)
        val ivPPForum: ImageView = view.findViewById(R.id.ivPPForum)
        val ivMedal: ImageView = view.findViewById(R.id.ivMedal)
        val ivMore: ImageView = view.findViewById(R.id.ivMore)
        val tvNama: TextView = view.findViewById(R.id.tvNama)
        val tvUsername: TextView = view.findViewById(R.id.tvUsername)
        val tvIsi: TextView = view.findViewById(R.id.tvIsi)
        val checkboxLike: CheckBox = view.findViewById(R.id.checkboxLike)
        val tvLikeCount: TextView = view.findViewById(R.id.tvLikeCount)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflatedView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycleritem_forum_komentar_user, parent, false)
        return MyViewHolder(inflatedView)
    }

    override fun getItemCount(): Int {
        return commentModels.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (position < commentModels.size) {
            val imgUri = commentModels[position].profile_image

            if (!imgUri.isNullOrEmpty()) {
                Picasso.get().invalidate(imgUri)
                Picasso.get().load(imgUri).into(holder.ivPPForum)
            } else {
                holder.ivPPForum.setImageResource(R.drawable.profile_profile)
            }

            if(commentModels[position].grade == "BRONZE" || commentModels[position].grade == ""){
                holder.ivMedal.setImageResource(R.drawable.bronze_medal)
            }
            if(commentModels[position].grade == "SILVER"){
                holder.ivMedal.setImageResource(R.drawable.silver_medal)
            }
            if(commentModels[position].grade == "GOLD"){
                holder.ivMedal.setImageResource(R.drawable.gold_medal)
            }

            holder.tvNama.text = commentModels[position].nama
            holder.tvUsername.text ="@" + commentModels[position].username
            holder.tvIsi.text = commentModels[position].message
            holder.tvLikeCount.text = commentModels[position].like_count.toString()

            holder.checkboxLike.isChecked = commentModels[position].liked == true

            ViewCompat.setElevation(holder.ivMedal,holder.cvPPForum.elevation * 2)

            holder.checkboxLike.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    likeComment(position)
                    val newLikeCount = commentModels[position].like_count?.plus(1)
                    commentModels[position].like_count = newLikeCount
                    holder.tvLikeCount.text = newLikeCount.toString()
                } else {
                    likeComment(position)
                    val newLikeCount = commentModels[position].like_count?.minus(1)
                    commentModels[position].like_count = newLikeCount
                    holder.tvLikeCount.text = newLikeCount.toString()
                }
            }

            holder.ivMore.setOnClickListener {
                if (commentModels[position].is_me == true){
                    Log.e("ID is_me:", commentModels[position].id.toString() )
                    showMoreDeleteDialog(position)
                }else{
                    showMoreReportDialog(position)
                }
            }
        }
    }

    fun updateComments(comments: ArrayList<CommentModel>) {
        commentModels.addAll(comments)
        notifyDataSetChanged()
    }

    fun likeComment(position: Int){
        val id = commentModels[position].id.toString()
        val tokenAuthenticator = TokenAuthenticator(prefManager)
        val okHttpClient = OkHttpClient.Builder()
            .authenticator(tokenAuthenticator)
            .build()
        val retrofit = Retro().getRetroForumUrl(okHttpClient)
        val service = retrofit.create(ForumService::class.java)
        val aToken = prefManager.getAccessToken()

        val call = service.likeComment(id,aToken)
        call.enqueue(object : Callback<FavResponse> {
            override fun onResponse(call: Call<FavResponse>, response: Response<FavResponse>) {
                if (response.isSuccessful) {
                    val rawResponse = response.raw().toString()
                    Log.e("Raw Response: ", rawResponse)
                } else {
                    val rawResponse = response.raw().toString()
                    Log.e("Raw Response: ", rawResponse)
                    Log.e("Error: ", "unsuccessful response")
                }
            }
            override fun onFailure(call: Call<FavResponse>, t: Throwable) {
                Log.e("Network API Error: ", t.message.toString())
                Log.e("Error: ","network or API call failure")
            }
        })
    }

    fun showMoreDeleteDialog(position: Int){
        Log.e("ID showMoreDeleteDialog:", commentModels[position].id.toString() )
        val dialog = Dialog(context, R.style.MaterialDialogSheet)
        dialog.setContentView(R.layout.dialog_more_comment)
        dialog.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.window?.attributes?.windowAnimations = R.style.MaterialDialogSheetAnimation
        dialog.show()

        val clMore: ConstraintLayout = dialog.findViewById(R.id.clMore)

        clMore.setOnClickListener {
            dialog.dismiss()
            showDeleteDialog(position)
        }
    }

    fun showMoreReportDialog(position: Int){
        val dialog = Dialog(context, R.style.MaterialDialogSheet)
        dialog.setContentView(R.layout.dialog_more_comment)
        dialog.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.window?.attributes?.windowAnimations = R.style.MaterialDialogSheetAnimation
        dialog.show()

        val clMore: ConstraintLayout = dialog.findViewById(R.id.clMore)
        val ivIcMore: ImageView = dialog.findViewById(R.id.ivIcMore)
        val tvMore: TextView = dialog.findViewById(R.id.tvMore)

        tvMore.text = "Laporkan Balasan"
        ivIcMore.setImageResource(R.drawable.ic_flag)

        clMore.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(context, LaporkanKomentarActivity::class.java)
            intent.putExtra("id", commentModels[position].id)
            activity.startActivity(intent)
            activity.finish()
        }
    }

    fun showDeleteDialog(position: Int){
        Log.e("ID showDeleteDialog:", commentModels[position].id.toString() )
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_delete_komentar)
        val btnHapus: AppCompatButton = dialog.findViewById(R.id.btnHapus)
        val btnKembali: TextView = dialog.findViewById(R.id.btnKembali)
        dialog.setTitle("Hapus Balasan")
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        btnHapus.setOnClickListener {
            dialog.dismiss()
            deleteComment(position)
        }
        btnKembali.setOnClickListener {
            dialog.dismiss()
        }
    }

    fun deleteComment(position: Int){
        Log.e("ID deleteComment:", commentModels[position].id.toString() )
        val id = commentModels[position].id.toString()
        val tokenAuthenticator = TokenAuthenticator(prefManager)
        val okHttpClient = OkHttpClient.Builder()
            .authenticator(tokenAuthenticator)
            .build()
        val retrofit = Retro().getRetroForumUrl(okHttpClient)
        val service = retrofit.create(ForumService::class.java)
        val aToken = prefManager.getAccessToken()

        val call = service.deleteComment(id,aToken)
        call.enqueue(object : Callback<FavResponse> {
            override fun onResponse(call: Call<FavResponse>, response: Response<FavResponse>) {
                if (response.isSuccessful) {
                    val rawResponse = response.raw().toString()
                    Log.e("Raw Response: ", rawResponse)
                    commentModels.removeAt(position)
                    notifyDataSetChanged()
                } else {
                    val rawResponse = response.raw().toString()
                    Log.e("Raw Response: ", rawResponse)
                    Log.e("Error: ", "unsuccessful response")
                }
            }
            override fun onFailure(call: Call<FavResponse>, t: Throwable) {
                Log.e("Network API Error: ", t.message.toString())
                Log.e("Error: ","network or API call failure")
            }
        })
    }
}