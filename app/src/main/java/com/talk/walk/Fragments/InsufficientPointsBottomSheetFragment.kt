package com.talk.walk.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.talk.walk.Activities.PointsActivity
import com.talk.walk.R
import com.talk.walk.Utils.Constants

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [InsuffienctPointsBottmSheetFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class InsuffienctPointsBottmSheetFragment : BottomSheetDialogFragment() {
    private val TAG: String? = InsuffienctPointsBottmSheetFragment::javaClass.name

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var mContext: Context

    private lateinit var bBuyPoints: Button
    private lateinit var tvCoinsMessage: TextView

    private lateinit var auth: FirebaseAuth
    private var database = Firebase.database
    private var databseReference = database.getReferenceFromUrl(Constants.Urls.DATABASE_URL)
    private lateinit var currentUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_insuffienct_points_bottm_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mContext = view.context

        bBuyPoints = view.findViewById(R.id.bBuyPoints)
        tvCoinsMessage = view.findViewById(R.id.tvCoinsMessage)

        auth = Firebase.auth
        currentUser = auth.currentUser!!

        tvCoinsMessage.text = param1

        bBuyPoints.setOnClickListener {
            val pointsIntent = Intent(mContext, PointsActivity::class.java)
            startActivity(pointsIntent)
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment InsuffienctPointsBottmSheetFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            InsuffienctPointsBottmSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialog;
    }

    override fun onStop() {
        super.onStop()
        dialog?.dismiss()
    }


}