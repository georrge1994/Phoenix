package com.phoenix.kspt.activites

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.gmail.samehadar.iosdialog.IOSDialog
import com.phoenix.kspt.R
import com.phoenix.kspt.firebase.FireBaseActivity
import com.phoenix.kspt.utils.HelpFunctions
import es.voghdev.pdfviewpager.library.RemotePDFViewPager
import es.voghdev.pdfviewpager.library.adapter.PDFPagerAdapter
import es.voghdev.pdfviewpager.library.remote.DownloadFile
import es.voghdev.pdfviewpager.library.util.FileUtil
import kotlinx.android.synthetic.main.activity_book_reader.*
import java.lang.Exception

/**
 * Created by darkt on 8/24/2017.
 */
const val BOOK_URL = "book_url"

/**
 * This activity load and show pdf document
 */
class PdfBookDisplayActivity : FireBaseActivity(), DownloadFile.Listener {
    private lateinit var remotePDFViewPager: RemotePDFViewPager
    private lateinit var waitingDialog: IOSDialog
    private var adapter: PDFPagerAdapter? = null
    private var url: String = ""
    private var countAttemptOpen = 0

    companion object {
        fun startActivity(context: Context, url: String) {
            val intent = Intent(context, PdfBookDisplayActivity::class.java)
            intent.putExtra(BOOK_URL, url)
            val options = ActivityOptions.makeCustomAnimation(context, R.anim.fade_in, R.anim.fade_out)
            context.startActivity(intent, options.toBundle())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_reader)

        url = intent.getStringExtra(BOOK_URL)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        waitingDialog =  HelpFunctions.initWaitingDialog(this, waitProgressbar)

        waitingDialog.show()
        showPdf(url)
    }

    /**
     * Upload and show a pdf file by url
     */
    private fun showPdf(url: String) {
        remotePDFViewPager = RemotePDFViewPager(this, url, this)
        remotePDFViewPager.id = R.id.pdfViewPager
    }

    override fun onSuccess(url: String?, destinationPath: String?) {
        adapter = PDFPagerAdapter(this, FileUtil.extractFileNameFromURL(url))
        remotePDFViewPager.adapter = adapter
        waitProgressbar.visibility = View.GONE
        waitProgressbar.stop()
        waitingDialog.hide()
        setContentView(remotePDFViewPager)
    }

    override fun onFailure(e: Exception?) {
        Toast.makeText(this, getString(R.string.Oops_error), Toast.LENGTH_SHORT).show()
        Toast.makeText(this, getString(R.string.Lets_try_again), Toast.LENGTH_SHORT).show()
        countAttemptOpen++

        if(countAttemptOpen < 3)
            showPdf(url)
        else
            onBackPressed()
    }

    override fun onProgressUpdate(progress: Int, total: Int) {
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter?.close()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.left_animation_enter, R.anim.left_animation_leave)
    }

}





