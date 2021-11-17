package com.example.criminalintentnew
import android.Manifest
import android.app.Activity
import android.app.ProgressDialog.show
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_PICK
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import androidx.lifecycle.Observer
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import android.view.Gravity.apply
import androidx.core.view.GravityCompat.apply
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import java.util.*
import android.database.Cursor
import android.graphics.BitmapFactory
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.MediaStore
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.image_dialog.*
import java.io.File


private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE = 0
private const val DATE_FORMAT="EEE, MMM, dd"
private const val REQUEST_CONTACT = 1
private const val REQUEST_PHOTO = 2
private const val PERMISSIONS_REQUEST_READ_CONTACTS = 1


class CrimeFragment: Fragment(), DatePickerFragment.Callbacks {
    private var widthPhoto: Int = 0
    private var heightPhoto: Int = 0


    private lateinit var crime : Crime
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri
    private lateinit var thiscontext : Context
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var callButton: Button
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView

    private var callbacks: CrimeFragment.CallbacksDelete?= null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as CrimeFragment.CallbacksDelete?
    }

    override fun onDetach() {
        super.onDetach()

        requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        callbacks = null
    }



    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProviders.of(this).get(CrimeDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        val crimeId: UUID = arguments?.getSerializable("crime_id") as UUID
        Log.d("CrimeFragment", "args bundle crime ID: $crimeId")
        crimeDetailViewModel.loadCrime(crimeId)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)
        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_date) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
        reportButton = view.findViewById(R.id.crime_report) as Button
        suspectButton = view.findViewById(R.id.crime_suspect) as Button
        callButton = view.findViewById(R.id.crime_call) as Button
        thiscontext = container?.getContext()!!
        photoButton = view.findViewById(R.id.crime_camera) as ImageButton
        photoView = view.findViewById(R.id.crime_photo) as ImageView
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeDetailViewModel.crimeLiveData.observe(
            viewLifecycleOwner,
             Observer { crime ->
                crime?.let{
                    this.crime = crime
                    photoFile = crimeDetailViewModel.getPhotoFile(crime)
                    photoUri = FileProvider.getUriForFile(requireActivity(),
                        "com.example.criminalintentnew.fileprovider",
                        photoFile)
                    updateUI()
                }
            })
    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }


    override fun onStart() {
        super.onStart()

        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(
                sequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int) {

            }

            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int) {
                crime.title = sequence.toString()
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        }

        titleField.addTextChangedListener(titleWatcher)

        solvedCheckBox.apply{
            setOnCheckedChangeListener{_, isChecked ->
                crime.isSolved = isChecked
            }
        }

        photoView.viewTreeObserver
            .addOnGlobalLayoutListener {
            widthPhoto = photoView.width
            heightPhoto = photoView.height
        }

        photoButton.apply {
            val packageManager: PackageManager = requireActivity().packageManager

            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolveActivity: ResolveInfo? = packageManager.resolveActivity(captureImage,
                PackageManager.MATCH_DEFAULT_ONLY)
            if(resolveActivity == null)
                isEnabled = false

            setOnClickListener {
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

                val cameraActivities: List<ResolveInfo> =
                    packageManager.queryIntentActivities(captureImage,
                    PackageManager.MATCH_DEFAULT_ONLY)

                for(cameraActivity in cameraActivities){
                    requireActivity().grantUriPermission(
                        cameraActivity.activityInfo.packageName,
                        photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }

                startActivityForResult(captureImage, REQUEST_PHOTO)
            }

        }

        dateButton.setOnClickListener{
            DatePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_DATE)
                show(this@CrimeFragment.requireFragmentManager(), DIALOG_DATE)
            }
        }

        photoView.setOnClickListener {

            if(photoFile.exists()) {
                ImageDialog.newInstance(photoFile.path).apply {
                    show(this@CrimeFragment.requireFragmentManager(), "ImageDialog")
                }
            }

        }

        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type="text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    getString(R.string.crime_reportsubject)
                )
            }.also {intent ->
                startActivity(intent)
//                val chooserIntent =
//                    Intent.createChooser(intent, getString(R.string.send_report))
//                startActivity(chooserIntent)
            }
        }

        suspectButton.apply {
            val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)

            setOnClickListener {
                startActivityForResult(pickContactIntent, REQUEST_CONTACT)

                if (ContextCompat.checkSelfPermission(thiscontext,
                        Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(thiscontext,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    val permissions = arrayOf(android.Manifest.permission.READ_CONTACTS, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    ActivityCompat.requestPermissions(requireActivity(), permissions,0)
                }

                callButton.isEnabled = true
            }

        }


        callButton.setOnClickListener {
            if(crime.phoneSuspect.isNotEmpty()) {
                Log.d("phone", "test")
                var str = crime.phoneSuspect
                Log.d("phone", str.toString())
                var numberUser: Uri = Uri.parse("tel:${crime.phoneSuspect}")
                val intent: Intent = Intent(Intent.ACTION_DIAL, numberUser)
                startActivity(intent)
                callButton.isEnabled = true
            }
            else{
                callButton.isEnabled = false
            }
        }
        }


    private fun updateUI(){
        titleField.setText(crime.title)
        dateButton.text = crime.date.toString()
        solvedCheckBox.apply{
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }
        if(crime.suspect.isNotEmpty()){
            suspectButton.text = crime.suspect
        }
        if(widthPhoto != 0 && heightPhoto != 0)
            updatePhotoView(widthPhoto, heightPhoto)

    }

    override fun onDateSelected(date: Date) {
        crime.date = date
        updateUI()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.delete_crime -> {
                crimeDetailViewModel.deleteCrime(crime)
                Log.d("delete", "delete")
                callbacks?.crimeDelete(crime.id)
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    interface CallbacksDelete {
        fun crimeDelete(crimeId: UUID)
    }

    private fun getCrimeReport(): String{
        val solvedString = if(crime.isSolved){
            getString(R.string.crime_report_solved)
        }
        else{
            getString(R.string.crime_report_unsolved)
        }

        val dateSting = DateFormat.format(DATE_FORMAT, crime.date).toString()
        val suspect = if(crime.suspect.isBlank()){
            getString(R.string.crime_report_no_suspect)
        }
        else{
            getString(R.string.crime_report_suspect, crime.suspect)
        }
        return getString(R.string.crime_report, crime.title, dateSting, solvedString, suspect)
    }



    private fun updatePhotoView(width: Int, height: Int){
        if(photoFile.exists()){
            val bitmap = getScalesBitmap(photoFile.path, width, height)
            photoView.setImageBitmap(bitmap)
            Log.d("suspect", "rrrr")
        }
        else{
            photoView.setImageDrawable(null)
            Log.d("suspect", "rrr")
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when{
            resultCode != Activity.RESULT_OK -> {
                Log.d("suspect", "error")
                return
            }
            requestCode == REQUEST_CONTACT && data != null -> {
                Log.d("suspect", "start")
                val contactUri : Uri? = data.data
                val queryFiels = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                var idPerson: Int = 0
                val cursor = contactUri?.let {
                    requireActivity().contentResolver
                        .query(it, null, null, null, null)
                }
                    cursor?.use{
                    if(it.count == 0) return
                    it.moveToFirst()
                    val nameFieldColumnIndex = it.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                    val idFieldColumnIndex = it.getColumnIndex(ContactsContract.PhoneLookup._ID)
                    val suspect = it.getString(nameFieldColumnIndex)
                        idPerson = it.getInt(idFieldColumnIndex)
                    Log.d("suspect", idFieldColumnIndex.toString())
                    crime.suspect = suspect
                    crime.idSuspect = idPerson

                        crimeDetailViewModel.saveCrime(crime)
                        Log.d("suspect", suspect)
                                suspectButton.text = suspect
                    }
                val cursorPhone = contactUri?.let {
                    requireActivity().contentResolver
                        .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + idPerson, null, null)
                }
                cursorPhone?.use{
                    if(it.count == 0) return
                    it.moveToFirst()
                    val numberFieldColumnIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val suspect = it.getString(numberFieldColumnIndex)
                    Log.d("suspect", suspect)
                    crime.phoneSuspect = suspect

                    crimeDetailViewModel.saveCrime(crime)

                }

            }
            requestCode == REQUEST_PHOTO -> {
                requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

                if(widthPhoto != 0 && heightPhoto != 0)
                    updatePhotoView(widthPhoto, heightPhoto)

            }

        }
    }



    companion object{
        fun newInstance(crimeId: UUID): CrimeFragment{
            val args = Bundle().apply {
                putSerializable("crime_id", crimeId)
            }
            return CrimeFragment().apply {
                arguments = args
            }
        }
    }
}