package com.spozebra.dhl_licenseplate_tag_poc

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import com.zebra.rfid.api3.*
import java.math.BigInteger

private val TAG: String = "InventoryFragment"
/**
 * A simple [Fragment] subclass.
 * Use the [InventoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class InventoryFragment : Fragment(), RfidEventsListener {


    private lateinit var adapter: ArrayAdapter<String>;
    private lateinit var listView: ListView
    private var tagList : ArrayList<String> = ArrayList();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainActivity.rfidInterface!!.listener = this

        listView = requireView().findViewById(R.id.tag_listview)
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, tagList)
        listView.adapter = adapter
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inventory, container, false)
    }


    // Read Event Notification
    override fun eventReadNotify(e: RfidReadEvents) {
        // Recommended to use new method getReadTagsEx for better performance in case of large tag population
        val myTags: Array<TagData> = MainActivity.rfidInterface!!.reader.Actions.getReadTags(100)
        if (myTags != null) {
            for (tag in myTags) {
                Log.d(TAG, "Tag ID " + tag.tagID)

                    this.requireActivity().runOnUiThread {
                        tagList.add(0, getLicensePlateFromTagConent(tag.tagID))
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }


    // Status Event Notification
    override fun eventStatusNotify(rfidStatusEvents: RfidStatusEvents) {
        Log.d(TAG, "Status Notification: " + rfidStatusEvents.StatusEventData.statusEventType)
        if (rfidStatusEvents.StatusEventData.statusEventType === STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT) {
            if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.handheldEvent === HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) {

                Thread {
                    try {
                            MainActivity.rfidInterface!!.reader.Actions.Inventory.perform()
                            Toast.makeText(this.context, "Inventory Started", Toast.LENGTH_SHORT).show()

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }.start()
            }
            if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.handheldEvent === HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED) {

                Thread {
                    try {
                        MainActivity.rfidInterface!!.reader.Actions.Inventory.stop()
                        Toast.makeText(this.context, "Inventory Stopped", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }.start()
            }
        }
    }

    private fun getLicensePlateFromTagConent(tagId : String) : String{

/*
        var tagContent = licensePlate

        if(licensePlate.startsWith("JJD")) {
            val tagSubstring = licensePlate.substring(3, licensePlate.length)
            val tagHexContent = BigInteger(tagSubstring).toString(16)
            tagContent = tagHexContent.padEnd(32, '0').uppercase()
        }*/

        return tagId
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment InventoryFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            InventoryFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}