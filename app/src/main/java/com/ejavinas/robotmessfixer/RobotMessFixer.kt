package com.ejavinas.robotmessfixer

/**
 * Notes:
 * - Papers should be inside folders, folders inside boxes
 * - Papers can be moved to a different folder
 * - Day 4: Empty containers are not added
 * - Day 5: If paper already exists and folder does not exist, don't move the paper
 * - Day 7: If an existing paper is grouped with new papers, use folder of existing paper
 * - Day 10: If an unmoved paper is grouped with moved paper, use folder of unmoved paper
 */
class RobotMessFixer {

    private val actualBoxes: HashMap<Int, Box> = HashMap()
    private val actualFolders: HashMap<Int, Folder> = HashMap()
    private val actualPapers: HashMap<Int, Paper> = HashMap()

    @Synchronized
    fun fixMess(input: List<String>): String {
        var currentBox: Box? = null
        var currentFolder: Folder? = null
        val inputBoxes = HashMap<Int, Box>()
        val inputFolders = HashMap<Int, Folder>()
        val inputPapers = HashMap<Int, Paper>()

        // Form a TreeMap-like structure first from inputs for easy accessing and logic checking
        for (i in input.size - 1 downTo 0) {
            val item = input[i]
            val type = item[0]
            val id = item.substring(1).toInt()
            when (type) {
                'B' -> {
                    val box = Box(id)
                    currentBox = box
                    currentFolder = null
                    inputBoxes[currentBox.id] = currentBox
                }
                'F' -> {
                    // check in case of malformed input (e.g. folder without box)
                    if (currentBox == null) continue

                    val folder = Folder(id)
                    currentFolder = folder
                    currentFolder.boxId = currentBox.id
                    currentBox.folderIds.add(currentFolder.id)
                    inputFolders[currentFolder.id] = currentFolder
                }
                'P' -> {
                    // check in case of malformed input (e.g. paper without box or folder)
                    if (currentBox == null) continue
                    if (currentFolder == null) continue

                    val paper = Paper(id)

                    paper.folderId = currentFolder.id
                    currentFolder.paperIds.add(paper.id)
                    inputPapers[paper.id] = paper
                }
                else -> throw IllegalArgumentException("Unknown item type: $type")
            }
        }

        // Fix mess with corresponding logics
        for ((folderId, folder) in inputFolders) {
            if (folder.paperIds.isEmpty()) {
                inputBoxes[folder.boxId]!!.folderIds.remove(folderId)
                continue
            }
            var realFolderId: Int? = null

            for (paperId in folder.paperIds) {
                if (actualPapers.containsKey(paperId) &&
                        actualPapers[paperId]!!.folderId != folderId) {
                    if (actualFolders.containsKey(folderId)) {
                        realFolderId = folderId
                    } else {
                        realFolderId = actualPapers[paperId]!!.folderId
                        inputBoxes[folder.boxId]!!.folderIds.remove(folderId)
                    }
                    break
                }
            }

            if (realFolderId == null) {
                actualFolders[folderId] = inputFolders[folderId]!!
                for (paperId in folder.paperIds) {
                    actualPapers[paperId] = inputPapers[paperId]!!
                }
            } else {
                for (paperId in folder.paperIds) {
                    if (actualPapers.containsKey(paperId) &&
                            actualPapers[paperId]!!.folderId != realFolderId) {
                        val paper = actualPapers[paperId]!!
                        actualFolders[paper.folderId]!!.let {
                            it.paperIds.remove(paperId)
                            if (it.paperIds.isEmpty()) {
                                actualFolders.remove(paper.folderId)
                                actualBoxes[it.boxId]!!.folderIds.remove(paper.folderId)
                                if (actualBoxes[it.boxId]!!.folderIds.isEmpty()) {
                                    actualBoxes.remove(it.boxId)
                                }
                            }
                        }
                        paper.folderId = realFolderId
                        actualFolders[realFolderId]!!.paperIds.add(paperId)
                    }
                    else {
                        val paper = inputPapers[paperId]!!
                        paper.folderId = realFolderId
                        actualPapers[paperId] = paper
                        actualFolders[realFolderId]!!.paperIds.add(paperId)
                    }
                }
            }
        }

        for ((boxId, box) in inputBoxes) {
            if (box.folderIds.isNotEmpty() && !actualBoxes.containsKey(boxId)) {
                actualBoxes[boxId] = box
            }
        }

        return getOutput()
    }

    private fun getOutput(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("[")
        for ((boxId, box) in actualBoxes) {
            for (folderId in box.folderIds) {
                for (paperId in actualFolders[folderId]!!.paperIds) {
                    stringBuilder.append("P${paperId},")
                }
                stringBuilder.append("F${folderId},")
            }
            stringBuilder.append("B${boxId},")
        }
        stringBuilder.deleteCharAt(stringBuilder.length-1)  // remove last comma
        stringBuilder.append("]")
        return stringBuilder.toString()
    }

    class Paper(
        val id: Int,
    ) {
        var folderId: Int? = null
        override fun toString(): String {
            return "Paper(id=$id, folder=${folderId})"
        }
    }

    class Folder(
        val id: Int,
        val paperIds: LinkedHashSet<Int> = LinkedHashSet()  // Used LinkedHashSet for fast lookup while retaining insertion order
    ) {
        var boxId: Int? = null

        override fun toString(): String {
            return "Folder(id=$id, box=$boxId, papers=${paperIds})"
        }
    }

    class Box(
        val id: Int,
        val folderIds: LinkedHashSet<Int> = LinkedHashSet() // Used LinkedHashSet for fast lookup while retaining insertion order
    ) {
        override fun toString(): String {
            return "Box(id=$id, folders=${folderIds})"
        }
    }

}