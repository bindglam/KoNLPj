package kr.ac.kaist.swrc.jhannanum.plugin.SupplementPlugin.PlainTextProcessor

import kr.ac.kaist.swrc.jhannanum.comm.PlainSentence

/**
 * This module prevent to process long Eojole before Morphlogical analysis by adding space between long Eojole.
 *
 */

class InformalEojeolSentenceFilter : PlainTextProcessor {
    /** As seen from class InformalEojeolSentenceFilter, the missing signatures are as follows.
     *  For convenience, these are usable as stub implementations.
     */
    // Members declared in kr.ac.kaist.swrc.jhannanum.plugin.SupplementPlugin.PlainTextProcessor.PlainTextProcessor
    var longEojoleLength = 20
    var smallestEojoleLen = 10

    override fun doProcess(ps: PlainSentence): PlainSentence {
        val buf = StringBuilder()
        val tokens = ps.getSentence().split("\\s+".toRegex())
        for (token in tokens) {
            if (token.length >= longEojoleLength) {
                for (i in 0 until token.length / smallestEojoleLen + 1) {
                    buf.append(token.slice(IntRange(i * smallestEojoleLen, (i + 1) * smallestEojoleLen - 1)))
                    buf.append(" ")
                }
            } else {
                buf.append(token)
                buf.append(" ")
            }
        }
        ps.sentence = buf.toString()
        return ps
    }

    override fun flush(): PlainSentence? = null
    override fun hasRemainingData(): Boolean = false

    // Members declared in kr.ac.kaist.swrc.jhannanum.plugin.Plugin
    override fun initialize(x1: String, x2: String) {}
    override fun shutdown() {}
}