package kr.ac.kaist.swrc.jhannanum.plugin.SupplementPlugin.PlainTextProcessor.SentenceSegmentor2

import kr.ac.kaist.swrc.jhannanum.comm.PlainSentence
import kr.ac.kaist.swrc.jhannanum.plugin.SupplementPlugin.PlainTextProcessor.PlainTextProcessor
import java.text.BreakIterator

/**
 * @author gogamza
 *
 */

class SentenceSegmentor2 : PlainTextProcessor {
    /** the ID of the document */
    private var documentID: Int = 0

    /** the ID of the sentence */
    private var sentenceID: Int = 0

    /** the flag to check if there is remaining data in the input buffer */
    private var hasRemainingData: Boolean = false

    /** the buffer for storing intermediate results */
    private var bufRes: String? = null

    /** the buffer for storing the remaining part after one sentence returned */
    private var bufSents: String? = null

    /** the index of the buffer for storing the remaining part */
    private var bufEojeolsIdx: Int = 0

    /** the flag to check whether current sentence is the end of document */
    private var endOfDocument: Boolean = false

    private val sentIter: BreakIterator = BreakIterator.getSentenceInstance()

    override fun doProcess(ps: PlainSentence?): PlainSentence? {
        var sents: String? = null

        if (bufSents != null) {
            sents = bufSents
        } else {
            if (ps == null) {
                return null
            }

            if (documentID != ps.documentID) {
                documentID = ps.documentID
                sentenceID = 0
            }

            val str: String? = ps.sentence
            if (str == null) {
                return null
            }
            sents = str

            endOfDocument = ps.isEndOfDocument
        }

        sentIter.setText(sents)
        val start = sentIter.first()
        val end = sentIter.next()
        if (end != BreakIterator.DONE) {
            val rawSent = sents!!.substring(start, end).trim()
            val sent = rawSent.substring(start, rawSent.length - 1) + " " + rawSent.substring(rawSent.length - 1, rawSent.length)
            bufSents = sents.substring(end)
            sentenceID += 1
            hasRemainingData = bufSents?.trim()?.isNotEmpty() == true

            return PlainSentence(documentID, sentenceID - 1, !hasRemainingData && endOfDocument, sent)
        } else {
            bufSents = null
            hasRemainingData = false
            return null
        }
    }

    override fun hasRemainingData(): Boolean = hasRemainingData

    override fun flush(): PlainSentence? = null

    // Members declared in kr.ac.kaist.swrc.jhannanum.plugin.Plugin

    override fun initialize(x1: String, x2: String) {}

    override fun shutdown() {}
}