package kr.ac.kaist.swrc.jhannanum.plugin.SupplementPlugin.PlainTextProcessor

import kr.ac.kaist.swrc.jhannanum.comm.PlainSentence
import kr.pe.freesearch.jhannanum.comm.HiddenMarkovModel
import kr.pe.freesearch.jhannanum.comm.ViterbiAlgorithm
import java.io.BufferedReader
import java.io.InputStreamReader

class Spacing : PlainTextProcessor {
    /** As seen from class Spacing, the missing signatures are as follows.
     *  For convenience, these are usable as stub implementations.
     */
    val numState = 2
    val numChars = 4131 + 2

    var hmm = HiddenMarkovModel(numState, numChars)
    var charMap = hashMapOf<String, Int>().withDefault { numChars - 1 }
    var viterbi: ViterbiAlgorithm? = null

    // Members declared in kr.ac.kaist.swrc.jhannanum.plugin.SupplementPlugin.PlainTextProcessor.PlainTextProcessor
    override fun doProcess(ps: PlainSentence): PlainSentence {
        val chss = ps.sentence.toCharArray()
        val seq = arrayListOf<Int>()
        // char to seq idx
        for (i in ps.sentence) {
            seq.add(charMap[i.toString()] ?: (numChars - 1))
        }
        val buf = CharArray(seq.size * 2) { '\u0020' }
        val spaceSeq = viterbi?.let { it(seq, seq.size, 0) }?.second ?: listOf()

        var spacingIdx = 0
        for ((i, x) in spaceSeq.withIndex()) {
            if (x == 1) {
                buf[spacingIdx] = '\u0020'
                spacingIdx++
            }
            buf[spacingIdx] = chss[i]
            spacingIdx++
        }
        val strBuf = buf.sliceArray(0 until spacingIdx).concatToString()
        val strCl = strBuf.replace("\\s+".toRegex(), " ")
        ps.sentence = strCl
        return ps
    }

    override fun flush(): PlainSentence? = null
    override fun hasRemainingData(): Boolean = false

    // Members declared in kr.ac.kaist.swrc.jhannanum.plugin.Plugin
    override fun initialize(x1: String, x2: String) {
        val chIdxFile = this::class.java.getResourceAsStream("/char_idx.txt")!!
        val source = BufferedReader(InputStreamReader(chIdxFile, "UTF-8"))
        val lineIter = source.lineSequence()
        //기본값의 경우 마지막 컬럼에 위치한 기본 확률값을 사용한다.0.5:0.5
        //var char_map = Map[String,Int]().withDefaultValue(numChars - 1)

        for (i in lineIter) {
            val parLi = i.split("\t")
            charMap[parLi[0]] = parLi[1].toInt()
        }

        //공백은 마지막 인덱스로 ...
        //char_map += (" " -> 4790)
        val maxIdx = charMap.values.maxOrNull()!! + 1
        charMap[" "] = maxIdx

        val emissionFile = this::class.java.getResourceAsStream("/emissionProb.txt")!!
        val source2 = BufferedReader(InputStreamReader(emissionFile, "UTF-8"))
        val lineIter2 = source2.lineSequence()

        //hmm = new HiddenMarkovModel(2, 4792)

        for (i in lineIter2) {
            val parLi2 = i.split("\t")
            hmm.B[parLi2[0].toInt(), parLi2[1].toInt()] = parLi2[2].toDouble()
        }

        // 공백의 다음엔 띄어쓰기가 올 가능성이 희박하다.
        hmm.B[1, 4131] = 0.10
        hmm.B[0, 4131] = 0.90
        // 기본 발현 확률
        hmm.B[1, 4132] = 0.50
        hmm.B[0, 4132] = 0.50

        // 코퍼스로 부터 계산된 전이확률
        hmm.A[0, 0] = 0.5851609
        hmm.A[0, 1] = 0.41483907
        hmm.A[1, 0] = 0.9300953
        hmm.A[1, 1] = 0.06990471

        // 첫 state 확률
        hmm.Pi[0] = 0.5
        hmm.Pi[1] = 0.5

        viterbi = ViterbiAlgorithm(hmm)
    }

    override fun shutdown() {}
}