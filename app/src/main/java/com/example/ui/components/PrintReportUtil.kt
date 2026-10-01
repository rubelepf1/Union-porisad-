package com.example.ui.components

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.data.model.Citizen
import com.example.data.model.DocumentWithPages
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PrintReportUtil {

    fun generateHtmlReport(
        upName: String,
        entrepreneurName: String,
        upazilaDistrict: String,
        title: String,
        citizens: List<Citizen>,
        documentsForSingle: List<DocumentWithPages>? = null
    ): String {
        val todayStr = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.US).format(Date())

        val rowsHtml = StringBuilder()
        citizens.forEachIndexed { index, c ->
            rowsHtml.append(
                """
                <tr>
                    <td style="text-align: center;">${index + 1}</td>
                    <td><strong>${c.fileId}</strong></td>
                    <td>${c.fullName}</td>
                    <td>${c.fatherMotherName}</td>
                    <td>${c.mobileNumber}</td>
                    <td>ওয়ার্ড ${c.wardNo}, ${c.village}</td>
                    <td>${c.serviceType}</td>
                    <td><span class="status-badge">${c.status}</span></td>
                </tr>
                """.trimIndent()
            )
        }

        val docDetailsHtml = if (documentsForSingle != null && documentsForSingle.isNotEmpty()) {
            val docListHtml = StringBuilder()
            documentsForSingle.forEach { doc ->
                val statusText = if (doc.documentItem.isUploaded) "✓ সংগৃহীত (${doc.pages.size} পৃষ্ঠা)" else "○ সংগ্রহ বাকি"
                val statusColor = if (doc.documentItem.isUploaded) "#1B5E20" else "#C62828"
                docListHtml.append(
                    """
                    <li style="margin-bottom: 4px;">
                        <strong>${doc.documentItem.documentType}:</strong> 
                        <span style="color: $statusColor; font-weight: bold;">$statusText</span>
                    </li>
                    """.trimIndent()
                )
            }
            """
            <div style="margin-top: 15px; border-top: 1px dashed #ccc; padding-top: 10px;">
                <h4 style="margin: 0 0 8px 0; color: #1B5E20;">সংযুক্ত কাগজপত্রের বিস্তারিত বিবরণ:</h4>
                <ul style="margin: 0; padding-left: 20px;">
                    $docListHtml
                </ul>
            </div>
            """.trimIndent()
        } else ""

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>$title</title>
            <style>
                body {
                    font-family: 'SolaimanLipi', 'Bangla', 'Noto Sans Bengali', Arial, sans-serif;
                    padding: 20px;
                    color: #212121;
                    line-height: 1.4;
                }
                .header {
                    text-align: center;
                    border-bottom: 2px solid #1B5E20;
                    padding-bottom: 12px;
                    margin-bottom: 16px;
                }
                .header h2 { margin: 0; color: #1B5E20; font-size: 20px; }
                .header h3 { margin: 4px 0; font-size: 15px; color: #424242; }
                .meta-table {
                    width: 100%;
                    margin-bottom: 14px;
                    font-size: 13px;
                }
                table.data-table {
                    width: 100%;
                    border-collapse: collapse;
                    margin-top: 10px;
                    font-size: 12px;
                }
                table.data-table th, table.data-table td {
                    border: 1px solid #BDBDBD;
                    padding: 6px 8px;
                    text-align: left;
                }
                table.data-table th {
                    background-color: #E8F5E9;
                    color: #1B5E20;
                    font-weight: bold;
                }
                .status-badge {
                    display: inline-block;
                    padding: 2px 6px;
                    border-radius: 4px;
                    background-color: #F1F5F2;
                    font-weight: bold;
                    font-size: 11px;
                }
                .signatures {
                    margin-top: 40px;
                    display: flex;
                    justify-content: space-between;
                }
                .sig-box {
                    width: 200px;
                    border-top: 1px solid #333;
                    text-align: center;
                    padding-top: 4px;
                    font-size: 12px;
                    float: left;
                }
                .sig-box-right {
                    width: 200px;
                    border-top: 1px solid #333;
                    text-align: center;
                    padding-top: 4px;
                    font-size: 12px;
                    float: right;
                }
                .clearfix::after {
                    content: "";
                    clear: both;
                    display: table;
                }
            </style>
        </head>
        <body>
            <div class="header">
                <h2>$upName</h2>
                <h3>$upazilaDistrict</h3>
                <div style="font-weight: bold; margin-top: 4px; color: #00695C;">জন্ম নিবন্ধন ও নাগরিক সেবা ফাইল ম্যানেজমেন্ট রিপোর্ট</div>
            </div>

            <table class="meta-table">
                <tr>
                    <td><strong>বিষয়:</strong> $title</td>
                    <td style="text-align: right;"><strong>তারিখ:</strong> $todayStr</td>
                </tr>
                <tr>
                    <td><strong>প্রস্তুতকারী উদ্যোক্তা:</strong> $entrepreneurName</td>
                    <td style="text-align: right;"><strong>মোট ফাইল সংখ্যা:</strong> ${citizens.size} টি</td>
                </tr>
            </table>

            <table class="data-table">
                <thead>
                    <tr>
                        <th style="width: 30px;">নং</th>
                        <th>ফাইল আইডি</th>
                        <th>নাগরিকের নাম</th>
                        <th>পিতা/মাতার নাম</th>
                        <th>মোবাইল</th>
                        <th>ঠিকানা</th>
                        <th>কাজের ধরন</th>
                        <th>স্ট্যাটাস</th>
                    </tr>
                </thead>
                <tbody>
                    $rowsHtml
                </tbody>
            </table>

            $docDetailsHtml

            <div style="margin-top: 50px;" class="clearfix">
                <div class="sig-box">
                    উদ্যোক্তার স্বাক্ষর<br>
                    <small>$entrepreneurName</small>
                </div>
                <div class="sig-box" style="margin-left: 40px;">
                    ইউপি সচিবের স্বাক্ষর<br>
                    <small>সীলসহ</small>
                </div>
                <div class="sig-box-right">
                    ইউপি চেয়ারম্যানের স্বাক্ষর<br>
                    <small>সীলসহ</small>
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    /**
     * Sends formatted HTML directly to Android's native Print Spooler.
     * The user can print to a physical printer or choose "Save as PDF".
     */
    fun printDocument(
        context: Context,
        jobName: String,
        htmlContent: String
    ) {
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                val printAdapter = webView.createPrintDocumentAdapter(jobName)
                val printAttributes = PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build()
                printManager?.print(jobName, printAdapter, printAttributes)
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }
}
