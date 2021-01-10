package com.example.androidjetpackdemo.workmanager

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.work.*
import com.example.androidjetpackdemo.MainFragment.Companion.KEY_BUNDLE
import com.example.androidjetpackdemo.R
import com.example.androidjetpackdemo.databinding.FragmentWorkManagerBinding
import java.util.concurrent.TimeUnit

class WorkManagerFragment : Fragment() {

    companion object {
        const val TAG = "WorkManager"
        const val WORK_REQUEST_DATA_KEY = "WORK_REQUEST_DATA_KEY"
        const val WORKER_DATA_KEY = "WORKER_DATA_KEY"
    }

    /**
     * Step 1 : 自定义Worker，需要重写 doWork() 实现具体业务逻辑
     */
    private lateinit var alarmWorker: AlarmWorker

    /**
     * Step 2 : 设置约束条件 constraints
     */
    private val constraints by lazy {
        Constraints.Builder().apply {
            // true 表示充电时执行，默认为 false
            setRequiresCharging(true)
            // true 表示空闲时运行，默认为 false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setRequiresDeviceIdle(false)
            }
            // 设置在哪些网络状态下运行，NOT_REQUIRED表示不需要网络
            setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            // 设置是否尽在电量充足时运行，默认为 false
            setRequiresBatteryNotLow(true)
            // 设置是否尽在内存充足时运行，默认为 false
            setRequiresStorageNotLow(true)
        }.build()
    }

    /**
     * Step 3 : 把 自定义Worker 及 constraints 设置给 WorkRequest
     */
    private val oneTimeWorkRequest by lazy {
        OneTimeWorkRequest.Builder(AlarmWorker::class.java)
            // 设置约束条件
            .setConstraints(constraints)
            // 设置延迟10秒后执行
            .setInitialDelay(10, TimeUnit.SECONDS)
            // 假如Worker执行出现了异常，比如服务器宕机，
            // 那么可以在Worker的doWork()方法中返回Result.retry()，让Worker过一段时间重试，
            // 系统会有默认的指数退避策略，也可以自定义
//            .setBackoffCriteria(BackoffPolicy.LINEAR, OneTimeWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
            // 设置 TAG 标签，后续可以通过 TAG 去跟踪或取消worker
            .addTag("AlarmOneTimeTag")
            // 可以通过setInputData()方法向Worker传递数据
            .setInputData(Data.Builder().putString(WORK_REQUEST_DATA_KEY,"one_time").build())
            .build()
    }

    /**
     * Step 4 : WorkManager.enqueue(oneTimeWorkRequest) 把 WorkRequest 放入执行队列
     */
    private val workManager by lazy { context?.let { WorkManager.getInstance(it) } }

    private lateinit var binding: FragmentWorkManagerBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val stringFromMainFragment = arguments?.getString(KEY_BUNDLE, "NO DATA")
        Log.d(TAG, "onCreateView: $stringFromMainFragment")

        binding = FragmentWorkManagerBinding.inflate(inflater).apply { initButton() }
        return binding.root
    }

    private fun FragmentWorkManagerBinding.initButton() {
        Log.d(TAG, "initButton: ")
        oneTimeBtn.setOnClickListener {
            Log.d(TAG, "click one_time_btn")
            val oneTimeWorkRequest = OneTimeWorkRequest
                .Builder(AlarmWorker::class.java)
                .setConstraints(constraints)
                .setInitialDelay(3, TimeUnit.SECONDS)
                .addTag("AlarmOneTimeTag")
                .setInputData(Data.Builder().putString(WORK_REQUEST_DATA_KEY,"one_time").build())
                .build()
            workManager?.enqueue(oneTimeWorkRequest)
            workManager?.observerWorkInfo(oneTimeWorkRequest)
        }
        periodicTimeBtn.setOnClickListener {
            Log.d(TAG, "click periodic_time_btn")
            val periodicWorkRequest = PeriodicWorkRequest
                // 最小时间间隔是 15 分钟,如果设置了1秒，源码也会改为15分钟
                .Builder(AlarmWorker::class.java, 1, TimeUnit.SECONDS)
                .setConstraints(constraints)
                .setInitialDelay(3, TimeUnit.SECONDS)
                .addTag("AlarmOneTimeTag")
                .setInputData(Data.Builder().putString(WORK_REQUEST_DATA_KEY, "periodic_time").build())
                .build()
            workManager?.enqueue(periodicWorkRequest)
            workManager?.observerWorkInfo(periodicWorkRequest)
        }

        orderBtn.setOnClickListener {
            Log.d(TAG, "click order_btn")
            val firstWorkRequest = OneTimeWorkRequest
                .Builder(AlarmWorker::class.java)
                .setConstraints(constraints)
                .addTag("AlarmOneTimeTag")
                .setInputData(Data.Builder().putString(WORK_REQUEST_DATA_KEY,"first").build())
                .build()
            val secondWorkRequest = OneTimeWorkRequest
                .Builder(AlarmWorker::class.java)
                .setConstraints(constraints)
                .addTag("AlarmOneTimeTag")
                .setInputData(Data.Builder().putString(WORK_REQUEST_DATA_KEY,"second").build())
                .build()
            val thirdWorkRequest = OneTimeWorkRequest
                .Builder(AlarmWorker::class.java)
                .setConstraints(constraints)
                .addTag("AlarmOneTimeTag")
                .setInputData(Data.Builder().putString(WORK_REQUEST_DATA_KEY,"third").build())
                .build()
            // 1 -> 2 -> 3
            workManager?.beginWith(firstWorkRequest)?.then(secondWorkRequest)?.then(thirdWorkRequest)?.enqueue()
            // 1,2 -> 3
//            workManager?.beginWith(listOf(firstWorkRequest, secondWorkRequest))?.then(thirdWorkRequest)?.enqueue()
            // 1 -> 2,3
//            workManager?.beginWith(firstWorkRequest)?.then(listOf(secondWorkRequest, thirdWorkRequest))?.enqueue()

            workManager?.observerWorkInfo(firstWorkRequest)
            workManager?.observerWorkInfo(secondWorkRequest)
            workManager?.observerWorkInfo(thirdWorkRequest)
        }

        cancelAllWorkerBtn.setOnClickListener {
            Log.d(TAG, "cancel_all_worker_btn")
            workManager?.cancelAllWork()
        }
    }

    /**
     * 获知任务的状态
     * getWorkInfoById / getWorkInfosByTag / getWorkInfosForUniqueWork
     */
    private fun WorkManager.observerWorkInfo(workRequest: WorkRequest) {
        takeIf { activity is LifecycleOwner}.apply {
            getWorkInfoByIdLiveData(workRequest.id).observe(activity as LifecycleOwner, Observer {
                // PS : 由于 periodicWorkRequest 会一直执行，不会收到 success data
                it?.apply {
                    Log.d(
                        TAG,
                        "workManager getData: " +
                                "state = $state , " +
                                "message = ${outputData.getString(WORKER_DATA_KEY)}")
                }
            })
        }

    }

}