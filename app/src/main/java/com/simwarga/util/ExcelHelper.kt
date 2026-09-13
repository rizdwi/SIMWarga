package com.simwarga.util

import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.InputStream
import java.io.OutputStream
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.xml.parsers.SAXParserFactory

data class WilayahMetadata(
    val desa: String = "Sukamaju",
    val kecamatan: String = "Cilodong",
    val kabupaten: String = "Kota Depok",
    val alamat: String = "Jl. Mawar No. 12",
    val rt: String = "001",
    val rw: String = "002",
    val tahun: String = "2024"
)

object ExcelHelper {

    // 18 Kolom Standar SIMWarga (Key Model)
    val COLUMNS = listOf(
        "NO_KK",
        "ALAMAT",
        "RT",
        "RW",
        "KELURAHAN",
        "KECAMATAN",
        "KABUPATEN",
        "NIK",
        "NAMA_LENGKAP",
        "JENIS_KELAMIN",
        "TEMPAT_LAHIR",
        "TANGGAL_LAHIR",
        "STATUS_HUBUNGAN",
        "AGAMA",
        "PENDIDIKAN",
        "PEKERJAAN",
        "STATUS_PERKAWINAN",
        "KEWARGANEGARAAN"
    )

    fun colNameToIndex(col: String): Int {
        var result = 0
        for (c in col.uppercase()) {
            if (c in 'A'..'Z') {
                result = result * 26 + (c - 'A' + 1)
            }
        }
        return result - 1
    }

    fun indexToColName(index: Int): String {
        var num = index
        val sb = StringBuilder()
        while (num >= 0) {
            sb.append(('A'.code + (num % 26)).toChar())
            num = num / 26 - 1
        }
        return sb.reverse().toString()
    }

    /**
     * Menulis data ke berkas OpenXML (.xlsx) dengan susunan persis format acuan RT 03 RW 07
     * Termasuk Judul Wilayah (Baris 1-2), Header bertingkat (Baris 4-5), dan baris data (Baris 6..N)
     */
    fun writeXlsx(
        outputStream: OutputStream,
        rows: List<Map<String, String>>,
        metadata: WilayahMetadata = WilayahMetadata()
    ) {
        val zos = ZipOutputStream(outputStream)

        // 1. [Content_Types].xml
        zos.putNextEntry(ZipEntry("[Content_Types].xml"))
        val contentTypes = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
</Types>"""
        zos.write(contentTypes.toByteArray(Charsets.UTF_8))
        zos.closeEntry()

        // 2. _rels/.rels
        zos.putNextEntry(ZipEntry("_rels/.rels"))
        val rootRels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""
        zos.write(rootRels.toByteArray(Charsets.UTF_8))
        zos.closeEntry()

        // 3. xl/_rels/workbook.xml.rels
        zos.putNextEntry(ZipEntry("xl/_rels/workbook.xml.rels"))
        val wbRels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""
        zos.write(wbRels.toByteArray(Charsets.UTF_8))
        zos.closeEntry()

        // 4. xl/workbook.xml
        zos.putNextEntry(ZipEntry("xl/workbook.xml"))
        val workbookXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="DAPTAR JIWA USIK" sheetId="1" r:id="rId1"/>
  </sheets>
</workbook>"""
        zos.write(workbookXml.toByteArray(Charsets.UTF_8))
        zos.closeEntry()

        // 5. xl/styles.xml
        zos.putNextEntry(ZipEntry("xl/styles.xml"))
        val stylesXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <fonts count="1"><font><sz val="11"/><name val="Calibri"/></font></fonts>
  <fills count="1"><fill><patternFill patternType="none"/></fill></fills>
  <borders count="1"><border/></borders>
  <cellStyleXfs count="1"><xf/></cellStyleXfs>
  <cellXfs count="1"><xf/></cellXfs>
</styleSheet>"""
        zos.write(stylesXml.toByteArray(Charsets.UTF_8))
        zos.closeEntry()

        // 6. xl/worksheets/sheet1.xml
        zos.putNextEntry(ZipEntry("xl/worksheets/sheet1.xml"))
        val sheetHeader = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <sheetData>"""
        zos.write(sheetHeader.toByteArray(Charsets.UTF_8))

        // Baris 1: Informasi Desa, Kecamatan, Kabupaten
        val row1Text = "DESA ${metadata.desa} - KEC. ${metadata.kecamatan} - KAB. ${metadata.kabupaten}"
        val row1Xml = """
    <row r="1">
      <c r="B1" t="inlineStr"><is><t>${xmlEscape(row1Text)}</t></is></c>
    </row>"""
        zos.write(row1Xml.toByteArray(Charsets.UTF_8))

        // Baris 2: Informasi Alamat, RT, RW, Tahun
        val row2Text = "${metadata.alamat} RT.${metadata.rt} RW.${metadata.rw}  TAHUN ${metadata.tahun}"
        val row2Xml = """
    <row r="2">
      <c r="B2" t="inlineStr"><is><t>${xmlEscape(row2Text)}</t></is></c>
    </row>"""
        zos.write(row2Xml.toByteArray(Charsets.UTF_8))

        // Baris 4: Header Tingkat 1 (Sama persis dengan file acuan user)
        val row4Xml = """
    <row r="4">
      <c r="B4" t="inlineStr"><is><t>No.</t></is></c>
      <c r="C4" t="inlineStr"><is><t>No.      Urut</t></is></c>
      <c r="D4" t="inlineStr"><is><t>KEPALA KELUARGA</t></is></c>
      <c r="E4" t="inlineStr"><is><t>ANGGOTA KELUARGA</t></is></c>
      <c r="F4" t="inlineStr"><is><t>JENIS KELAMIN</t></is></c>
      <c r="G4" t="inlineStr"><is><t>TEMPAT</t></is></c>
      <c r="H4" t="inlineStr"><is><t>TGL/BLN/THN</t></is></c>
      <c r="I4" t="inlineStr"><is><t>L</t></is></c>
      <c r="J4" t="inlineStr"><is><t>P</t></is></c>
      <c r="K4" t="inlineStr"><is><t>THN</t></is></c>
      <c r="L4" t="inlineStr"><is><t>UMUR</t></is></c>
      <c r="M4" t="inlineStr"><is><t>STATUS</t></is></c>
      <c r="N4" t="inlineStr"><is><t>Agama</t></is></c>
      <c r="O4" t="inlineStr"><is><t>Pendidikan Terakhir</t></is></c>
      <c r="P4" t="inlineStr"><is><t>Pekerjaan</t></is></c>
      <c r="Q4" t="inlineStr"><is><t>Nomor</t></is></c>
      <c r="R4" t="inlineStr"><is><t>Nomor</t></is></c>
      <c r="S4" t="inlineStr"><is><t>KETERANGAN</t></is></c>
      <c r="T4" t="inlineStr"><is><t>ORANG TUA</t></is></c>
      <c r="U4" t="inlineStr"><is><t>ORANG TUA</t></is></c>
    </row>"""
        zos.write(row4Xml.toByteArray(Charsets.UTF_8))

        // Baris 5: Header Tingkat 2 (Sub-header KTP, KK, AYAH, IBU)
        val row5Xml = """
    <row r="5">
      <c r="G5" t="inlineStr"><is><t>LAHIR</t></is></c>
      <c r="H5" t="inlineStr"><is><t>LAHIR</t></is></c>
      <c r="Q5" t="inlineStr"><is><t>KTP</t></is></c>
      <c r="R5" t="inlineStr"><is><t>KK</t></is></c>
      <c r="T5" t="inlineStr"><is><t>AYAH</t></is></c>
      <c r="U5" t="inlineStr"><is><t>IBU</t></is></c>
    </row>"""
        zos.write(row5Xml.toByteArray(Charsets.UTF_8))

        // Baris 6..N: Data Warga (dikelompokkan per KK)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        var kkUrut = 1
        var jiwaUrut = 1
        var currentRowNum = 6

        // Kelompokkan data berdasarkan No. KK
        val grouped = rows.groupBy { it["NO_KK"].orEmpty() }

        for ((noKk, anggotaList) in grouped) {
            var isFirstInKk = true

            for (warga in anggotaList) {
                val nama = warga["NAMA_LENGKAP"].orEmpty()
                val hub = warga["STATUS_HUBUNGAN"].orEmpty()
                val jk = if (warga["JENIS_KELAMIN"]?.uppercase()?.startsWith("P") == true) "P" else "L"
                val tglLahirStr = warga["TANGGAL_LAHIR"].orEmpty()
                val tglLahirMillis = DateHelper.parseDate(tglLahirStr)

                var thnLahir = ""
                var umur = ""
                if (tglLahirMillis != null && tglLahirMillis > 0L) {
                    val cal = Calendar.getInstance().apply { timeInMillis = tglLahirMillis }
                    val y = cal.get(Calendar.YEAR)
                    thnLahir = y.toString()
                    umur = (currentYear - y).coerceAtLeast(0).toString()
                }

                val rowSb = StringBuilder("\n    <row r=\"$currentRowNum\">")

                if (isFirstInKk || hub.equals("Kepala Keluarga", ignoreCase = true)) {
                    // Baris Kepala Keluarga
                    rowSb.append("<c r=\"B$currentRowNum\" t=\"inlineStr\"><is><t>$kkUrut</t></is></c>")
                    rowSb.append("<c r=\"C$currentRowNum\" t=\"inlineStr\"><is><t>$jiwaUrut</t></is></c>")
                    rowSb.append("<c r=\"D$currentRowNum\" t=\"inlineStr\"><is><t>${xmlEscape(nama)}</t></is></c>")
                    rowSb.append("<c r=\"R$currentRowNum\" t=\"inlineStr\"><is><t>${xmlEscape(noKk)}</t></is></c>")
                    isFirstInKk = false
                    kkUrut++
                } else {
                    // Baris Anggota Keluarga
                    rowSb.append("<c r=\"C$currentRowNum\" t=\"inlineStr\"><is><t>$jiwaUrut</t></is></c>")
                    rowSb.append("<c r=\"E$currentRowNum\" t=\"inlineStr\"><is><t>${xmlEscape(nama)}</t></is></c>")
                }

                // Kolom F s/d U
                rowSb.append("<c r=\"F$currentRowNum\" t=\"inlineStr\"><is><t>$jk</t></is></c>")
                rowSb.append("<c r=\"G$currentRowNum\" t=\"inlineStr\"><is><t>${xmlEscape(warga["TEMPAT_LAHIR"].orEmpty())}</t></is></c>")
                rowSb.append("<c r=\"H$currentRowNum\" t=\"inlineStr\"><is><t>${xmlEscape(tglLahirStr)}</t></is></c>")

                if (jk == "L") {
                    rowSb.append("<c r=\"I$currentRowNum\" t=\"inlineStr\"><is><t>1</t></is></c>")
                } else {
                    rowSb.append("<c r=\"J$currentRowNum\" t=\"inlineStr\"><is><t>1</t></is></c>")
                }

                if (thnLahir.isNotBlank()) rowSb.append("<c r=\"K$currentRowNum\" t=\"inlineStr\"><is><t>$thnLahir</t></is></c>")
                if (umur.isNotBlank()) rowSb.append("<c r=\"L$currentRowNum\" t=\"inlineStr\"><is><t>$umur</t></is></c>")

                rowSb.append("<c r=\"M$currentRowNum\" t=\"inlineStr\"><is><t>${xmlEscape(warga["STATUS_PERKAWINAN"].orEmpty())}</t></is></c>")
                rowSb.append("<c r=\"N$currentRowNum\" t=\"inlineStr\"><is><t>${xmlEscape(warga["AGAMA"].orEmpty())}</t></is></c>")
                rowSb.append("<c r=\"O$currentRowNum\" t=\"inlineStr\"><is><t>${xmlEscape(warga["PENDIDIKAN"].orEmpty())}</t></is></c>")
                rowSb.append("<c r=\"P$currentRowNum\" t=\"inlineStr\"><is><t>${xmlEscape(warga["PEKERJAAN"].orEmpty())}</t></is></c>")
                rowSb.append("<c r=\"Q$currentRowNum\" t=\"inlineStr\"><is><t>${xmlEscape(warga["NIK"].orEmpty())}</t></is></c>")

                rowSb.append("</row>")
                zos.write(rowSb.toString().toByteArray(Charsets.UTF_8))

                jiwaUrut++
                currentRowNum++
            }
        }

        val sheetFooter = "\n  </sheetData>\n</worksheet>"
        zos.write(sheetFooter.toByteArray(Charsets.UTF_8))
        zos.closeEntry()

        zos.finish()
        zos.flush()
    }

    private fun xmlEscape(input: String): String {
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    /**
     * Membaca berkas spreadsheet Excel (.xlsx) atau CSV
     * Cerdas mengenali format bertingkat RT/RW (Buku Induk Warga) maupun format tabel flat.
     */
    fun readSpreadsheet(inputStream: InputStream): List<Map<String, String>> {
        val bytes = inputStream.readBytes()
        if (bytes.isEmpty()) return emptyList()

        val isZip = bytes.size >= 4 &&
                bytes[0] == 0x50.toByte() &&
                bytes[1] == 0x4B.toByte() &&
                bytes[2] == 0x03.toByte() &&
                bytes[3] == 0x04.toByte()

        return if (isZip) {
            readXlsxFromBytes(bytes)
        } else {
            readCsvFromBytes(bytes)
        }
    }

    private fun readXlsxFromBytes(bytes: ByteArray): List<Map<String, String>> {
        val sharedStrings = mutableListOf<String>()
        var sheetBytes: ByteArray? = null

        val zis = ZipInputStream(bytes.inputStream())
        var entry = zis.nextEntry
        while (entry != null) {
            val name = entry.name.replace("\\", "/")
            if (name.equals("xl/sharedStrings.xml", ignoreCase = true)) {
                sharedStrings.addAll(parseSharedStrings(zis.readBytes()))
            } else if (name.matches(Regex("xl/worksheets/sheet[0-9]+\\.xml", RegexOption.IGNORE_CASE))) {
                if (sheetBytes == null) {
                    sheetBytes = zis.readBytes()
                }
            }
            zis.closeEntry()
            entry = zis.nextEntry
        }

        if (sheetBytes == null) return emptyList()

        return parseSheetXml(sheetBytes, sharedStrings)
    }

    private class SharedStringsHandler : DefaultHandler() {
        val strings = mutableListOf<String>()
        private var inSi = false
        private var inT = false
        private val buffer = StringBuilder()

        override fun startElement(uri: String?, localName: String?, qName: String, attributes: Attributes) {
            if (qName.equals("si", ignoreCase = true)) {
                inSi = true
                buffer.setLength(0)
            } else if (inSi && qName.equals("t", ignoreCase = true)) {
                inT = true
            }
        }

        override fun characters(ch: CharArray, start: Int, length: Int) {
            if (inT) buffer.append(ch, start, length)
        }

        override fun endElement(uri: String?, localName: String?, qName: String) {
            if (qName.equals("t", ignoreCase = true)) {
                inT = false
            } else if (qName.equals("si", ignoreCase = true)) {
                inSi = false
                strings.add(buffer.toString())
            }
        }
    }

    private fun parseSharedStrings(xmlBytes: ByteArray): List<String> {
        val handler = SharedStringsHandler()
        val factory = SAXParserFactory.newInstance()
        val parser = factory.newSAXParser()
        parser.parse(xmlBytes.inputStream(), handler)
        return handler.strings
    }

    private class SheetHandler(private val sharedStrings: List<String>) : DefaultHandler() {
        val rawRows = mutableMapOf<Int, MutableMap<Int, String>>()
        private var currentRowIndex = -1
        private var currentRowMap: MutableMap<Int, String>? = null
        private var currentCol = -1
        private var cellType: String? = null
        private var inValue = false
        private val buffer = StringBuilder()

        override fun startElement(uri: String?, localName: String?, qName: String, attributes: Attributes) {
            if (qName.equals("row", ignoreCase = true)) {
                currentRowIndex = attributes.getValue("r")?.toIntOrNull() ?: (rawRows.size + 1)
                currentRowMap = mutableMapOf()
            } else if (qName.equals("c", ignoreCase = true)) {
                val cellRef = attributes.getValue("r").orEmpty()
                val colLetters = cellRef.filter { it.isLetter() }
                currentCol = if (colLetters.isNotBlank()) ExcelHelper.colNameToIndex(colLetters) else -1
                cellType = attributes.getValue("t")
                buffer.setLength(0)
            } else if (qName.equals("v", ignoreCase = true) || qName.equals("t", ignoreCase = true)) {
                inValue = true
                buffer.setLength(0)
            }
        }

        override fun characters(ch: CharArray, start: Int, length: Int) {
            if (inValue) buffer.append(ch, start, length)
        }

        override fun endElement(uri: String?, localName: String?, qName: String) {
            if (qName.equals("v", ignoreCase = true) || qName.equals("t", ignoreCase = true)) {
                inValue = false
            } else if (qName.equals("c", ignoreCase = true)) {
                if (currentRowMap != null && currentCol >= 0) {
                    var text = buffer.toString()
                    if (cellType == "s") {
                        val idx = text.toIntOrNull()
                        if (idx != null && idx in sharedStrings.indices) {
                            text = sharedStrings[idx]
                        }
                    }
                    currentRowMap!![currentCol] = text
                }
            } else if (qName.equals("row", ignoreCase = true)) {
                if (currentRowMap != null && currentRowMap!!.isNotEmpty() && currentRowIndex > 0) {
                    rawRows[currentRowIndex] = currentRowMap!!
                }
                currentRowMap = null
            }
        }
    }

    private fun parseSheetXml(sheetBytes: ByteArray, sharedStrings: List<String>): List<Map<String, String>> {
        val handler = SheetHandler(sharedStrings)
        val factory = SAXParserFactory.newInstance()
        val parser = factory.newSAXParser()
        parser.parse(sheetBytes.inputStream(), handler)

        val rawRows = handler.rawRows
        if (rawRows.isEmpty()) return emptyList()

        // 1. Ekstrak Wilayah dari Baris 1 dan Baris 2 (jika ada)
        val meta = extractWilayahMetadata(rawRows)

        // 2. Deteksi apakah berkas menggunakan Format Acuan RT/RW (ada kolom KEPALA KELUARGA / ANGGOTA KELUARGA di Baris 4)
        val row4 = rawRows[4]
        val isRtRwFormat = row4 != null && row4.values.any { valStr ->
            valStr.contains("KEPALA KELUARGA", ignoreCase = true) ||
                    valStr.contains("ANGGOTA KELUARGA", ignoreCase = true)
        }

        return if (isRtRwFormat) {
            parseRtRwFormat(rawRows, meta)
        } else {
            parseFlatFormat(rawRows, meta)
        }
    }

    private fun extractWilayahMetadata(rawRows: Map<Int, Map<Int, String>>): WilayahMetadata {
        var desa = "Sukamaju"
        var kecamatan = "Cilodong"
        var kabupaten = "Kota Depok"
        var alamat = "Jl. Mawar No. 12"
        var rt = "001"
        var rw = "002"
        var tahun = "2024"

        // Cek Baris 1: e.g. "DESA BALONGGANDU - KEC. JATISARI - KAB. KARAWANG"
        val row1Text = rawRows[1]?.values?.joinToString(" ").orEmpty()
        if (row1Text.isNotBlank()) {
            val desaMatch = Regex("DESA\\s+([A-Za-z\\s]+?)(?=\\s*-|\\s*KEC|$)", RegexOption.IGNORE_CASE).find(row1Text)
            if (desaMatch != null) desa = desaMatch.groupValues[1].trim()

            val kecMatch = Regex("KEC[.\\s]+([A-Za-z\\s]+?)(?=\\s*-|\\s*KAB|$)", RegexOption.IGNORE_CASE).find(row1Text)
            if (kecMatch != null) kecamatan = kecMatch.groupValues[1].trim()

            val kabMatch = Regex("KAB[.\\s]+([A-Za-z\\s]+?)(?=\\s*-|$)", RegexOption.IGNORE_CASE).find(row1Text)
            if (kabMatch != null) kabupaten = kabMatch.groupValues[1].trim()
        }

        // Cek Baris 2: e.g. "PERUM BUMI CIKAMPEK BARU RT.03 RW.07  TAHUN 2023"
        val row2Text = rawRows[2]?.values?.joinToString(" ").orEmpty()
        if (row2Text.isNotBlank()) {
            val rtMatch = Regex("RT[.\\s]*([0-9]+)", RegexOption.IGNORE_CASE).find(row2Text)
            if (rtMatch != null) rt = rtMatch.groupValues[1].trim()

            val rwMatch = Regex("RW[.\\s]*([0-9]+)", RegexOption.IGNORE_CASE).find(row2Text)
            if (rwMatch != null) rw = rwMatch.groupValues[1].trim()

            val thnMatch = Regex("TAHUN\\s*([0-9]{4})", RegexOption.IGNORE_CASE).find(row2Text)
            if (thnMatch != null) tahun = thnMatch.groupValues[1].trim()

            val alamatMatch = Regex("^(.*?)(?=\\s*RT[.\\s]*[0-9]+)", RegexOption.IGNORE_CASE).find(row2Text)
            if (alamatMatch != null && alamatMatch.groupValues[1].isNotBlank()) {
                alamat = alamatMatch.groupValues[1].trim()
            }
        }

        return WilayahMetadata(desa, kecamatan, kabupaten, alamat, rt, rw, tahun)
    }

    /**
     * Mem-parsing format acuan RT/RW (Buku Induk Warga):
     * Kolom D: KEPALA KELUARGA
     * Kolom E: ANGGOTA KELUARGA
     * Kolom Q: Nomor KTP (NIK)
     * Kolom R: Nomor KK
     */
    private fun parseRtRwFormat(
        rawRows: Map<Int, Map<Int, String>>,
        meta: WilayahMetadata
    ): List<Map<String, String>> {
        val result = mutableListOf<Map<String, String>>()
        var activeNoKk = ""
        var tempKkCounter = 1

        val colD = colNameToIndex("D")
        val colE = colNameToIndex("E")
        val colF = colNameToIndex("F")
        val colG = colNameToIndex("G")
        val colH = colNameToIndex("H")
        val colM = colNameToIndex("M")
        val colN = colNameToIndex("N")
        val colO = colNameToIndex("O")
        val colP = colNameToIndex("P")
        val colQ = colNameToIndex("Q")
        val colR = colNameToIndex("R")

        // Mulai baris ke-6 (setelah header baris 4-5)
        val sortedRowIndices = rawRows.keys.filter { it >= 6 }.sorted()

        for (rowIndex in sortedRowIndices) {
            val row = rawRows[rowIndex] ?: continue

            val kepalaKeluarga = row[colD]?.trim().orEmpty()
            val anggotaKeluarga = row[colE]?.trim().orEmpty()
            val rawNik = row[colQ]?.trim().orEmpty()
            val rawNoKk = row[colR]?.trim().orEmpty()

            // Jika baris adalah baris statistik/rekapitulasi (mis. "KELOMPOK UMUR", "DATA INI DI BUAT"), lewati
            if (kepalaKeluarga.contains("KELOMPOK", ignoreCase = true) ||
                kepalaKeluarga.contains("UMUR", ignoreCase = true) ||
                kepalaKeluarga.contains("JUMLAH", ignoreCase = true) ||
                kepalaKeluarga.contains("DATA INI", ignoreCase = true) ||
                kepalaKeluarga.contains("TERTANDA", ignoreCase = true)
            ) {
                continue
            }

            // Bersihkan format NIK dan No. KK (hilangkan ".0" jika format angka float di Excel)
            val cleanNik = cleanIdNumber(rawNik)
            val cleanNoKkInRow = cleanIdNumber(rawNoKk)

            // Baris harus memiliki nama warga atau NIK
            if (kepalaKeluarga.isBlank() && anggotaKeluarga.isBlank() && cleanNik.isBlank()) {
                continue
            }

            val isNewKk = kepalaKeluarga.isNotBlank()
            val namaWarga: String
            val statusHubungan: String

            if (isNewKk) {
                namaWarga = kepalaKeluarga
                statusHubungan = "Kepala Keluarga"
                // Perbarui activeNoKk
                activeNoKk = if (cleanNoKkInRow.isNotBlank()) {
                    cleanNoKkInRow
                } else {
                    // Jika No. KK kosong (mis. baru pindah), buat No. KK placeholder dari NIK atau counter
                    if (cleanNik.length >= 10) cleanNik.take(10) + "000000"
                    else "321514000000" + String.format("%04d", tempKkCounter++)
                }
            } else {
                namaWarga = anggotaKeluarga
                val statusNikah = row[colM]?.trim().orEmpty()
                val jk = row[colF]?.trim().orEmpty().uppercase()

                statusHubungan = when {
                    statusNikah.equals("Kawin", ignoreCase = true) && (jk.startsWith("P") || jk == "2") -> "Istri"
                    statusNikah.equals("Belum Kawin", ignoreCase = true) -> "Anak"
                    else -> "Lainnya"
                }
            }

            // Normalisasi NIK jika ada perbedaan 1 digit ketikan
            val finalNik = when {
                cleanNik.length == 16 -> cleanNik
                cleanNik.length > 16 -> cleanNik.take(16)
                cleanNik.length in 10..15 -> cleanNik.padEnd(16, '0')
                else -> {
                    // Jika NIK kosong sama sekali, generate NIK unik sementara berbasis No. KK + urutan
                    if (activeNoKk.length == 16) activeNoKk.take(12) + String.format("%04d", rowIndex)
                    else "3215140000" + String.format("%06d", rowIndex)
                }
            }

            val jkVal = when (row[colF]?.trim()?.uppercase()) {
                "P", "PEREMPUAN", "WANITA", "2" -> "P"
                else -> "L"
            }

            val rowMap = mapOf(
                "NO_KK" to activeNoKk,
                "ALAMAT" to meta.alamat,
                "RT" to meta.rt,
                "RW" to meta.rw,
                "KELURAHAN" to meta.desa,
                "KECAMATAN" to meta.kecamatan,
                "KABUPATEN" to meta.kabupaten,
                "NIK" to finalNik,
                "NAMA_LENGKAP" to namaWarga,
                "JENIS_KELAMIN" to jkVal,
                "TEMPAT_LAHIR" to row[colG]?.trim().orEmpty(),
                "TANGGAL_LAHIR" to row[colH]?.trim().orEmpty(),
                "STATUS_HUBUNGAN" to statusHubungan,
                "AGAMA" to (row[colN]?.trim()?.ifBlank { "Islam" } ?: "Islam"),
                "PENDIDIKAN" to row[colO]?.trim().orEmpty(),
                "PEKERJAAN" to row[colP]?.trim().orEmpty(),
                "STATUS_PERKAWINAN" to (row[colM]?.trim()?.ifBlank { "Kawin" } ?: "Kawin"),
                "KEWARGANEGARAAN" to "WNI"
            )

            result.add(rowMap)
        }

        return result
    }

    /**
     * Mem-parsing format flat standar (1 baris header)
     */
    private fun parseFlatFormat(
        rawRows: Map<Int, Map<Int, String>>,
        meta: WilayahMetadata
    ): List<Map<String, String>> {
        val sortedIndices = rawRows.keys.sorted()
        if (sortedIndices.isEmpty()) return emptyList()

        // Cari baris header
        var headerIndex = sortedIndices.first()
        for (idx in sortedIndices.take(10)) {
            val r = rawRows[idx] ?: continue
            if (r.values.any { it.contains("KK", ignoreCase = true) || it.contains("NIK", ignoreCase = true) }) {
                headerIndex = idx
                break
            }
        }

        val headerRow = rawRows[headerIndex] ?: return emptyList()
        val headerMap = mutableMapOf<Int, String>()
        for ((colIdx, headerName) in headerRow) {
            val normalized = normalizeHeaderName(headerName)
            if (normalized.isNotBlank()) {
                headerMap[colIdx] = normalized
            }
        }

        val result = mutableListOf<Map<String, String>>()
        for (idx in sortedIndices) {
            if (idx <= headerIndex) continue
            val row = rawRows[idx] ?: continue
            val rowMap = mutableMapOf<String, String>()
            for ((colIdx, value) in row) {
                val colKey = headerMap[colIdx]
                if (colKey != null) {
                    rowMap[colKey] = value.trim()
                }
            }

            // Bersihkan NIK & NO_KK
            rowMap["NIK"] = cleanIdNumber(rowMap["NIK"].orEmpty())
            rowMap["NO_KK"] = cleanIdNumber(rowMap["NO_KK"].orEmpty())

            // Isi nilai default wilayah jika kosong
            if (rowMap["ALAMAT"].isNullOrBlank()) rowMap["ALAMAT"] = meta.alamat
            if (rowMap["RT"].isNullOrBlank()) rowMap["RT"] = meta.rt
            if (rowMap["RW"].isNullOrBlank()) rowMap["RW"] = meta.rw
            if (rowMap["KELURAHAN"].isNullOrBlank()) rowMap["KELURAHAN"] = meta.desa
            if (rowMap["KECAMATAN"].isNullOrBlank()) rowMap["KECAMATAN"] = meta.kecamatan
            if (rowMap["KABUPATEN"].isNullOrBlank()) rowMap["KABUPATEN"] = meta.kabupaten

            if (rowMap.values.any { it.isNotBlank() }) {
                result.add(rowMap)
            }
        }
        return result
    }

    private fun cleanIdNumber(raw: String): String {
        var clean = raw.trim()
        if (clean.endsWith(".0")) {
            clean = clean.substring(0, clean.length - 2)
        }
        return clean.filter { it.isDigit() }
    }

    private fun readCsvFromBytes(bytes: ByteArray): List<Map<String, String>> {
        val text = String(bytes, Charsets.UTF_8)
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()

        val firstLine = lines.first()
        val delimiter = if (firstLine.count { it == ';' } > firstLine.count { it == ',' }) ';' else ','

        val headerTokens = parseCsvLine(firstLine, delimiter).map { normalizeHeaderName(it) }
        val result = mutableListOf<Map<String, String>>()

        for (i in 1 until lines.size) {
            val tokens = parseCsvLine(lines[i], delimiter)
            val rowMap = mutableMapOf<String, String>()
            for (colIdx in tokens.indices) {
                if (colIdx < headerTokens.size) {
                    val key = headerTokens[colIdx]
                    if (key.isNotBlank()) {
                        rowMap[key] = tokens[colIdx].trim()
                    }
                }
            }
            if (rowMap.values.any { it.isNotBlank() }) {
                rowMap["NIK"] = cleanIdNumber(rowMap["NIK"].orEmpty())
                rowMap["NO_KK"] = cleanIdNumber(rowMap["NO_KK"].orEmpty())
                result.add(rowMap)
            }
        }
        return result
    }

    private fun parseCsvLine(line: String, delimiter: Char): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false

        for (c in line) {
            if (c == '\"') {
                inQuotes = !inQuotes
            } else if (c == delimiter && !inQuotes) {
                tokens.add(sb.toString())
                sb.setLength(0)
            } else {
                sb.append(c)
            }
        }
        tokens.add(sb.toString())
        return tokens
    }

    fun normalizeHeaderName(raw: String): String {
        val clean = raw.trim()
            .uppercase()
            .replace(" ", "_")
            .replace(".", "")
            .replace("-", "_")

        return when {
            clean in listOf("NO_KK", "NOKK", "NOMOR_KK", "KARTU_KELUARGA", "KK") -> "NO_KK"
            clean in listOf("ALAMAT", "ALAMAT_RUMAH", "ALAMAT_LENGKAP") -> "ALAMAT"
            clean == "RT" -> "RT"
            clean == "RW" -> "RW"
            clean in listOf("KELURAHAN", "DESA", "KELURAHAN_DESA") -> "KELURAHAN"
            clean in listOf("KECAMATAN", "KEC") -> "KECAMATAN"
            clean in listOf("KABUPATEN", "KOTA", "KABUPATEN_KOTA", "KAB") -> "KABUPATEN"
            clean in listOf("NIK", "NO_NIK", "NOMOR_INDUK_KEPENDUDUKAN", "KTP", "NOMOR_KTP") -> "NIK"
            clean in listOf("NAMA", "NAMA_LENGKAP", "NAMA_WARGA", "KEPALA_KELUARGA", "ANGGOTA_KELUARGA") -> "NAMA_LENGKAP"
            clean in listOf("JENIS_KELAMIN", "JK", "KELAMIN") -> "JENIS_KELAMIN"
            clean in listOf("TEMPAT_LAHIR", "TMP_LAHIR", "TEMPAT") -> "TEMPAT_LAHIR"
            clean in listOf("TANGGAL_LAHIR", "TGL_LAHIR", "TGL/BLN/THN") -> "TANGGAL_LAHIR"
            clean in listOf("STATUS_HUBUNGAN", "HUBUNGAN", "HUB_KELUARGA", "SHDK") -> "STATUS_HUBUNGAN"
            clean == "AGAMA" -> "AGAMA"
            clean in listOf("PENDIDIKAN", "PENDIDIKAN_TERAKHIR") -> "PENDIDIKAN"
            clean in listOf("PEKERJAAN", "PROFESI") -> "PEKERJAAN"
            clean in listOf("STATUS_PERKAWINAN", "STATUS_NIKAH", "STATUS") -> "STATUS_PERKAWINAN"
            clean in listOf("KEWARGANEGARAAN", "WARGANEGARA") -> "KEWARGANEGARAAN"
            else -> clean
        }
    }

    /**
     * Menghasilkan baris template contoh untuk pengurus RT/RW
     */
    fun createSampleTemplateRows(): List<Map<String, String>> {
        return listOf(
            mapOf(
                "NO_KK" to "3201012304050001",
                "ALAMAT" to "Jl. Mawar No. 12",
                "RT" to "001",
                "RW" to "002",
                "KELURAHAN" to "Sukamaju",
                "KECAMATAN" to "Cilodong",
                "KABUPATEN" to "Kota Depok",
                "NIK" to "3201011508900001",
                "NAMA_LENGKAP" to "Budi Santoso",
                "JENIS_KELAMIN" to "L",
                "TEMPAT_LAHIR" to "Depok",
                "TANGGAL_LAHIR" to "15/08/1990",
                "STATUS_HUBUNGAN" to "Kepala Keluarga",
                "AGAMA" to "Islam",
                "PENDIDIKAN" to "SLTA",
                "PEKERJAAN" to "Karyawan Swasta",
                "STATUS_PERKAWINAN" to "Kawin",
                "KEWARGANEGARAAN" to "WNI"
            ),
            mapOf(
                "NO_KK" to "3201012304050001",
                "ALAMAT" to "Jl. Mawar No. 12",
                "RT" to "001",
                "RW" to "002",
                "KELURAHAN" to "Sukamaju",
                "KECAMATAN" to "Cilodong",
                "KABUPATEN" to "Kota Depok",
                "NIK" to "3201014605920002",
                "NAMA_LENGKAP" to "Siti Rahmawati",
                "JENIS_KELAMIN" to "P",
                "TEMPAT_LAHIR" to "Bogor",
                "TANGGAL_LAHIR" to "20/05/1992",
                "STATUS_HUBUNGAN" to "Istri",
                "AGAMA" to "Islam",
                "PENDIDIKAN" to "SLTA",
                "PEKERJAAN" to "Ibu Rumah Tangga",
                "STATUS_PERKAWINAN" to "Kawin",
                "KEWARGANEGARAAN" to "WNI"
            ),
            mapOf(
                "NO_KK" to "3201012304050001",
                "ALAMAT" to "Jl. Mawar No. 12",
                "RT" to "001",
                "RW" to "002",
                "KELURAHAN" to "Sukamaju",
                "KECAMATAN" to "Cilodong",
                "KABUPATEN" to "Kota Depok",
                "NIK" to "3201012608180001",
                "NAMA_LENGKAP" to "Faris Pratama",
                "JENIS_KELAMIN" to "L",
                "TEMPAT_LAHIR" to "Depok",
                "TANGGAL_LAHIR" to "26/08/2018",
                "STATUS_HUBUNGAN" to "Anak",
                "AGAMA" to "Islam",
                "PENDIDIKAN" to "Belum Sekolah",
                "PEKERJAAN" to "Belum Bekerja",
                "STATUS_PERKAWINAN" to "Belum Kawin",
                "KEWARGANEGARAAN" to "WNI"
            )
        )
    }
}
