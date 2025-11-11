package com.alexportfolio.uniparser.service

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class CleanupService(private val fs: FileService, private val rs: ResultService) {

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    fun removeOldResults(){
        rs.findOldResults().let { oldResMap->
            fs.markResultsForRemoval(oldResMap).let { markedList->
                if(markedList.isNotEmpty() && rs.deleteBatchByIds(markedList)>0)
                    fs.removeMarkedResults()
            }
        }
    }
}