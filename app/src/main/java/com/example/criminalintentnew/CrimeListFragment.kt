package com.example.criminalintentnew

import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_crime_list.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.emptyList


private const val TAG = "CrimeListFragment"


class CrimeListFragment: Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    interface Callbacks {
        fun onCrimeSelected(crimeId: UUID)
    }

    private var callbacks: Callbacks?= null
    private lateinit var crimeRecyclerView: RecyclerView

    private var adapter: CrimeAdapter ?= CrimeAdapter(emptyList())

    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProviders.of(this).get(CrimeListViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)

        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view) as RecyclerView
        crimeRecyclerView.layoutManager = LinearLayoutManager(context)
        crimeRecyclerView.adapter = adapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tvEmpty = view.findViewById(R.id.emptyList) as TextView
        crimeListViewModel.crimeListLiveData.observe(
            viewLifecycleOwner,
            Observer{ crimes ->
                if(crimes.isEmpty()){
                    tvEmpty.visibility = View.VISIBLE
                }
                else{
                    tvEmpty.visibility = View.GONE
                    updateUI(crimes)
                }
            }
        )
    }

    private inner class CrimeHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener{

        private lateinit var crime: Crime
        private val titleTextView : TextView = itemView.findViewById(R.id.crime_title)
        private val dateTextView : TextView = itemView.findViewById(R.id.crime_date)
        private val solvedImageView: ImageView = itemView.findViewById(R.id.crime_solved_image)


        fun bind(crime: Crime){
            this.crime = crime
            titleTextView.text = this.crime.title
            val dateFormat = SimpleDateFormat("EEEE, MMM dd, yyyy")
            dateTextView.text = dateFormat.format(this.crime.date)
            solvedImageView.visibility = if(crime.isSolved)
                View.VISIBLE
            else
                View.GONE
        }
        init {
            itemView.setOnClickListener(this)

        }

        override fun onClick(v: View){
            callbacks?.onCrimeSelected(crime.id)
        }


    }

    private inner class CrimeHolderPolice(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener{

        private lateinit var crime: Crime
        private val titleTextView : TextView = itemView.findViewById(R.id.crime_title)
        private val dateTextView : TextView = itemView.findViewById(R.id.crime_date)
        private val btnPolice: Button = itemView.findViewById(R.id.btnNeedPolice)
        private val solvedImageView: ImageView = itemView.findViewById(R.id.crime_solved_image)


        fun bind(crime: Crime){
            this.crime = crime
            titleTextView.text = this.crime.title
            val dateFormat = SimpleDateFormat("EEEE, MMM dd, yyyy")
            dateTextView.text = dateFormat.format(this.crime.date)
            solvedImageView.visibility = if(crime.isSolved)
                View.VISIBLE
            else
                View.GONE
        }

        init {
            itemView.setOnClickListener(this)

        }

        override fun onClick(v: View){
            callbacks?.onCrimeSelected(crime.id)
        }


    }


    private inner class CrimeAdapter(var crimes: List<Crime>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

            return if(viewType==0) {
                val view = layoutInflater.inflate(R.layout.list_item_crime, parent, false)
                CrimeHolder(view)
            } else {
                val view = layoutInflater.inflate(R.layout.list_item_crime_need_police, parent, false)
                CrimeHolderPolice(view)

            }
        }

        override fun getItemViewType(position: Int): Int {
            return crimes[position].requiresPolice
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val crime = crimes[position]
            when (holder.getItemViewType()) {
                0 -> {
                    val crimeHolder: CrimeHolder = holder as CrimeHolder
                    crimeHolder.bind(crime)
                }
                1 ->{
                    val crimeHolder: CrimeHolderPolice = holder as CrimeHolderPolice
                    crimeHolder.bind(crime)
                }
            }

        }

            override fun getItemCount() = crimes.size

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.new_crime -> {
                val crime = Crime()
                crimeListViewModel.addCrime(crime)
                callbacks?.onCrimeSelected(crime.id)
                true
            }
            else -> return  super.onOptionsItemSelected(item)
        }
    }
    private fun updateUI(crimes: List<Crime>){
        adapter = CrimeAdapter(crimes)
        crimeRecyclerView.adapter = adapter
    }

    companion object{
        fun newInstance():CrimeListFragment{
            return CrimeListFragment()
        }
    }
}