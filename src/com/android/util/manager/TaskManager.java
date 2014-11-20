package com.android.util.manager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TaskManager extends BaseManager {

    /** 任务超时时间*/
    public static final long TASK_TIMEOUT = 60000;
    private ConcurrentLinkedQueue<ITask> taskQueues;
    private ConcurrentHashMap<Integer, ITask> taskMaps;
    
    TaskManager() {
        taskQueues = new ConcurrentLinkedQueue<ITask>();
        taskMaps = new ConcurrentHashMap<Integer, ITask>();
    }
    
    /** 生成一个实例工厂*/
    public static  TaskManager newFactory() {
        return new TaskManager();
    }
    
    @Override
    public void init() {
        taskQueues.clear();
        taskMaps.clear();
    }

    @Override
    public void destory() {
        taskQueues.clear();
        taskMaps.clear();
    }
    
    public synchronized boolean addTask(ITask task) {
        if(task == null) return false;
        int key = task.getKey();
        if(!taskMaps.containsKey(key)) {
            taskMaps.put(key, task);
            taskQueues.offer(task);
            return true;
        }else {
            return false;
        }
    }
    
    public synchronized boolean addTask(ITask task, boolean nextable) {
        if(task == null) return false;
        boolean r = addTask(task);
        if(nextable && r) {
            notifyNext();
        }
        return r;
    }
    
    public synchronized void cancelTask(int taskKey) {
        if(taskMaps.containsKey(taskKey)) {
            ITask task = taskMaps.remove(taskKey);
            taskQueues.remove(task);
            
            notifyNext();
        }
    }
    
    public synchronized void notifyNext() {
        if(!taskQueues.isEmpty()) {
            ITask task = taskQueues.peek();
            if(task.getState() == TState.END) {
                // 已完成
                taskQueues.poll();
                taskMaps.remove(task.getKey());
                startTask();
            }else 
            if(task.getState() == TState.UNSTART) {
                // 未开始
                startTask();
            }else {
                // 进行中,是否超时了，否则让给下一个任务
                if(task.isTimeOut(TASK_TIMEOUT)) {
                    taskQueues.poll();
                    taskMaps.remove(task.getKey());
                    startTask();
                }
            }
        }
    }
    
    private void startTask() {
        ITask task = taskQueues.peek();
        try {
            if(task != null) {
                task.preExcute();
                task.excute();
            }
        }catch(Exception e) {
            e.printStackTrace();
            // 作废进行下一个
            taskQueues.poll();
            taskMaps.remove(task.getKey());
            notifyNext();
        }
    }
    
    public static enum TState{
        UNSTART, RUNNING, END
    }
    
    public static abstract class  ITask{
        private TState state = TState.UNSTART;
        protected long excuteTime;
        private TaskManager manager;
        
        public ITask(TaskManager manager) {
             this.manager = manager;
        }
        
        public abstract void excute();
        
        /** 获取这个任何的唯一key，默认为这个对象的hashCode*/
        public int getKey() {
            return this.hashCode();
        }
        
        void preExcute()
        {
            state = TState.RUNNING;
            excuteTime = System.currentTimeMillis();
        }
        
        TState getState() {
            return state;
        }
        
        boolean isTimeOut(long timeout) {
            return System.currentTimeMillis() - excuteTime > timeout;
        }
        
        public void setEnd() {
            state = TState.END;
            manager.notifyNext();
        }
    }
}