/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.student.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.LayoutDirection
import android.view.*
import androidx.core.text.TextUtilsCompat
import com.instructure.annotations.CanvasPdfMenuGrouping

import com.instructure.student.R
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.setGone
import com.pspdfkit.document.processor.PdfProcessorTask
import com.pspdfkit.document.sharing.DefaultDocumentSharingController
import com.pspdfkit.document.sharing.DocumentSharingIntentHelper
import com.pspdfkit.document.sharing.DocumentSharingManager
import com.pspdfkit.document.sharing.SharingOptions
import com.pspdfkit.ui.PdfActivity
import com.pspdfkit.ui.toolbar.AnnotationCreationToolbar
import com.pspdfkit.ui.toolbar.ContextualToolbar
import com.pspdfkit.ui.toolbar.ContextualToolbarMenuItem
import com.pspdfkit.ui.toolbar.ToolbarCoordinatorLayout
import java.util.*

class CandroidPSPDFActivity : PdfActivity(), ToolbarCoordinatorLayout.OnContextualToolbarLifecycleListener {
    override fun onDisplayContextualToolbar(p0: ContextualToolbar<*>) {}
    override fun onRemoveContextualToolbar(p0: ContextualToolbar<*>) {}

    private var menuItems: List<ContextualToolbarMenuItem>? = null

    override fun onPrepareContextualToolbar(toolbar: ContextualToolbar<*>) {
        if(toolbar is AnnotationCreationToolbar) {
            toolbar.setMenuItemGroupingRule(CanvasPdfMenuGrouping(this))
            toolbar.layoutParams = ToolbarCoordinatorLayout.LayoutParams(
                    ToolbarCoordinatorLayout.LayoutParams.Position.TOP, EnumSet.of(ToolbarCoordinatorLayout.LayoutParams.Position.TOP))
        }

        // We need to reverse the order of the menu items for RTL, once PSPDFKit supports RTL this can be deleted
        if (menuItems == null) {
                menuItems = toolbar.menuItems
            if (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == LayoutDirection.RTL) {
                for (item in menuItems!!) {
                    if (item.position == ContextualToolbarMenuItem.Position.START) {
                        item.position = ContextualToolbarMenuItem.Position.END
                    } else {
                        item.position = ContextualToolbarMenuItem.Position.START
                    }
                }
                if(toolbar.closeButton.position == ContextualToolbarMenuItem.Position.START) {
                    toolbar.closeButton.position = ContextualToolbarMenuItem.Position.END
                } else {
                    toolbar.closeButton.position = ContextualToolbarMenuItem.Position.START
                }
            }
        } else {
            toolbar.menuItems = menuItems as List<ContextualToolbarMenuItem>
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setOnContextualToolbarLifecycleListener(this)

        ViewStyler.themeActionBar(this, supportActionBar, ThemePrefs.primaryColor)

        findViewById<View>(R.id.pspdf__activity_thumbnail_bar)?.setBackgroundColor(ThemePrefs.brandColor)
        findViewById<View>(R.id.pspdf__activity_title_overlay)?.setBackgroundColor(ThemePrefs.brandColor)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        menuInflater.inflate(R.menu.pspdf_activity_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        return if (item.itemId == R.id.upload_item) {
            uploadDocumentToCanvas()
            true
        } else {
            false
        }
    }

    private fun uploadDocumentToCanvas() {
        if (document != null) {
            DocumentSharingManager.shareDocument(
                    CandroidDocumentSharingController(this),
                    document,
                    SharingOptions(PdfProcessorTask.AnnotationProcessingMode.FLATTEN))
        }
    }


    private inner class CandroidDocumentSharingController(private val mContext: Context) : DefaultDocumentSharingController(mContext) {

        override fun onDocumentPrepared(shareUri: Uri) {
            val intent = Intent(mContext, ShareFileUploadActivity::class.java)
            intent.type = DocumentSharingIntentHelper.MIME_TYPE_PDF
            intent.putExtra(Intent.EXTRA_STREAM, shareUri)
            intent.action = Intent.ACTION_SEND
            mContext.startActivity(intent)
        }
    }
}
