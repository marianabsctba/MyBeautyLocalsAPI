package br.infnet.marianabs.mylocals.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import br.infnet.marianabs.mylocals.R
import br.infnet.marianabs.mylocals.model.Results
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.lang.StringBuilder

class NearbyAdapter(private  val nearbyList: ArrayList<Results>) :
    RecyclerView.Adapter<NearbyAdapter.NearbyViewHolder>() {

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NearbyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.template_nearby_item,
            parent,
            false)
        return NearbyViewHolder(itemView, mListener)
    }

    override fun onBindViewHolder(holder: NearbyViewHolder , position: Int) {
        val currentItem = nearbyList[position]

        val photoReference = currentItem.photos?.get(0)?.photo_reference
        val photoUrl = StringBuilder("https://maps.googleapis.com/maps/api/place/photo")
        photoUrl.append("?maxwidth=400")
        photoUrl.append("&photo_reference=$photoReference")
        photoUrl.append("&key=${holder.itemView.context.getString(R.string.browser_key)}")

        Glide.with(holder.itemView.context).load(photoUrl.toString()).into(holder.thumbnailImage)
        holder.placeName.text = currentItem.name
        if(currentItem.opening_hours?.open_now == true) {
            holder.openStatus.text = "Open Now"
            holder.openStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.avocado_green))
        } else {
            holder.openStatus.text = "Closed"
            holder.openStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.red_500))
        }
        holder.rateNumber.text = currentItem.rating.toString()

        val db = Firebase.firestore
        db.collection("halalPlaces")
            .document(currentItem.place_id!!)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("SNAPSHOT_ERROR", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    holder.halalIcon.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.green_500))
                    holder.halalStatus.text = "Halal"
                } else {
                    holder.halalIcon.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.gray_400))
                    holder.halalStatus.text = "Not verified"
                }
            }
    }

    override fun getItemCount(): Int {
        return nearbyList.size
    }

    class NearbyViewHolder(itemView: View, listener: onItemClickListener): RecyclerView.ViewHolder(itemView) {

        val thumbnailImage: ImageView = itemView.findViewById(R.id.thumbnail_image)
        val placeName: TextView = itemView.findViewById(R.id.place_name)
        val openStatus: TextView = itemView.findViewById(R.id.open_status)
        val rateNumber: TextView = itemView.findViewById(R.id.rate_number)
        val halalIcon: ImageView = itemView.findViewById(R.id.halal_icon)
        val halalStatus: TextView = itemView.findViewById(R.id.halal_status)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

}