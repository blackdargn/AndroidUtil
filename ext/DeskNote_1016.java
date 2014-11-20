
HTTP Proxy{
    ////////////////////////////////////////////////////////
    -->blackdargn002
    
    117.141.115.141:8080
    110.173.49.18:3128
    218.93.112.72:3128
    124.47.57.2:9000            -->谢老师007 3315756 blackling001@163.com 0
    
    121.9.231.82:9999
    221.122.79.52:3128          -->浪人neo Q1992315 blackling002@163.com 1
    
    211.138.121.37:84
    219.150.242.54:9999         -->003   blackling003@163.com 2
    
    122.227.228.118:8118        -->004   blackling004@163.com 3
    
    114.66.219.90:8080
    222.174.27.166:8080         -->005   blackling005@163.com 4
    116.252.253.121:8080        -->006	 blackling006@163.com x0
    
	  114.112.63.117:3128
	  121.12.249.120:9999
    222.240.213.196:9999 
    183.63.131.18:9999          -->007   blackling007@163.com x1
    
    221.182.62.115:9999
    110.179.184.4:3128 
    114.80.136.220:7780
    123.103.58.59:8090
    123.103.58.60:3128          -->008   blackling008@163.com x2
             
    61.144.79.188:9999
    58.215.52.146:8080
    117.36.231.239:9999   		  -->009   blackling009@163.com x3
    
    221.122.79.51:3128				  -->13715042214@163.com
    221.122.79.52:3128
    
    118.114.77.116:8080
    
    ////////////////////////////////////////////////////////
}

linux TCP{
    注意不要屏蔽ssh 22端口.
    
            /etc/sysctl.conf
            /sbin/sysctl -p
          
            vm.min_free_kbytes = 65536
            vm.swappiness = 0
            ########################################################
            net.core.rmem_max = 2097152
            net.core.wmem_max = 2097152
            net.core.netdev_max_backlog = 8096
            
            fs.file-max = 1048576
            
            net.ipv4.tcp_fin_timeout = 15
            net.ipv4.tcp_keepalive_time = 30
            net.ipv4.tcp_syncookies = 1
            net.ipv4.tcp_tw_reuse = 1
            net.ipv4.tcp_tw_recycle = 1
            net.ipv4.tcp_max_syn_backlog = 8192
            net.ipv4.tcp_rmem = 4096 4096 33554432
            net.ipv4.tcp_wmem = 4096 4096 33554432
            net.ipv4.tcp_mem = 786432 1048576 26777216
            net.ipv4.tcp_max_tw_buckets = 5000
            net.ipv4.ip_local_port_range = 1024 65535
            net.ipv4.route.gc_timeout = 100
            net.ipv4.tcp_syn_retries = 1
            net.ipv4.tcp_synack_retries = 1
            ########################################################
            一、 文件数限制修改
            (1) vi /etc/security/limits.conf
            在末尾追加
            * soft nofile 10240
            * hard nofile 10240
             
            (2) vi /etc/pam.d/login
            在末尾追加
            session required /lib/security/pam_limits.so
                      
            ab -c 100 -n 5000 http://127.0.0.1:8080/
            webbench -t 30 -c 200 http://127.0.0.1:8080/
            rj6tp7RX4Hw6
            /etc/sysconfig/iptables
            service iptables restart
            
            #允许单个IP的最大连接数为 30    
            iptables -I INPUT -p tcp --dport 80 -m connlimit --connlimit-above 30 -j REJECT
            
            #一瞬间太多的连接访问，导致服务器呈现呆滞状态。这时，就需要下列的三行指令：
            
            -I INPUT -p tcp --dport 82 -d 118.123.11.140 -m connlimit --connlimit-above 30 -j REJECT -m state --state NEW -m recent --name httpuser --set
            -A INPUT -m recent --update --name httpuser --seconds 60 --hitcount 9 -j LOG --log-prefix  'HTTP attack:  '
            -A INPUT -m recent --update --name httpuser --seconds 60 --hitcount 9 -j DROP
            
            其中 SERVER_IP 换上被攻击的服务器 IP。
            
            ///////Nginx//////////////////////////////////////////////////////////
            HttpLimitZoneModule配置来限制ip在同一时间段的访问次数来防cc攻击。
            HttpLimitReqModul用来限制连单位时间内连接数的模块，使用limit_req_zone和limit_req指令配合使用来达到限制。
            一旦并发连接超过指定数量，就会返回503错误。
            HttpLimitConnModul用来限制单个ip的并发连接数，使用limit_zone和limit_conn指令
            这两个模块的区别前一个是对一段时间内的连接数限制，后者是对同一时刻的连接数限制
            ////////////////////////////////////////////////////////////////////////
            HttpLimitReqModul 限制某一段时间内同一ip访问数实例
            http{
                ...

                #定义一个名为allips的limit_req_zone用来存储session，大小是10M内存，
                #以$binary_remote_addr 为key,限制平均每秒的请求为20个，
                #1M能存储16000个状态，rete的值必须为整数，
                #如果限制两秒钟一个请求，可以设置成30r/m

                limit_req_zone $binary_remote_addr zone=allips:10m rate=20r/s;
                ...
                server{
                    ...
                    location {
                        ...

                        #限制每ip每秒不超过20个请求，漏桶数burst为5
                        #brust的意思就是，如果第1秒、2,3,4秒请求为19个，
                        #第5秒的请求为25个是被允许的。
                        #但是如果你第1秒就25个请求，第2秒超过20的请求返回503错误。
                        #nodelay，如果不设置该选项，严格使用平均速率限制请求数，
                        #第1秒25个请求时，5个请求放到第2秒执行，
                        #设置nodelay，25个请求将在第1秒执行。

                        limit_req zone=allips burst=5 nodelay;
                        ...
                    }
                    ...
                }
                ...
            }
            ///////////////////////////////////////////////////////////////////////////////
            HttpLimitZoneModule 限制并发连接数实例
            limit_zone只能定义在http作用域，limit_conn可以定义在http server location作用域
    
            http{
            ...
    
            #定义一个名为one的limit_zone,大小10M内存来存储session，
            #以$binary_remote_addr 为key
            #nginx 1.18以后用limit_conn_zone替换了limit_conn
            #且只能放在http作用域
            limit_conn_zone   one  $binary_remote_addr  10m;  
            ...
            server{
                ...
                location {
                    ...
                   limit_conn one 20;          #连接数限制
    
                   #带宽限制,对单个连接限数，如果一个ip两个连接，就是500x2k
                   limit_rate 500k;            
    
                    ...
                }
                ...
            }
            ...
        }
        ///////////////////////////////////////////////////////////////////
}

1.横竖屏切换时候activity的生命周期 android:configChanges
{
1、不设置Activity的android:configChanges时，
	切屏会重新调用各个生命周期，切横屏时会执行一次，切竖屏时会执行两次
 
2、设置Activity的android:configChanges="orientation"时，
	切屏还是会重新调用各个生命周期，切横、竖屏时只会执行一次
 
3、设置Activity的android:configChanges="orientation|keyboardHidden"时，
	切屏不会重新调用各个生命周期，只会执行onConfigurationChanged方法
}

0.onInterceptTouchEvent
{
由于onInterceptTouchEvent()的机制比较复杂，上面的说明写的也比较复杂，总结一下，基本的规则是：

1.   down事件首先会传递到onInterceptTouchEvent()方法

2.   如果该ViewGroup的onInterceptTouchEvent()在接收到down事件处理完成之后return false，那么后续的move, up等事件将继续会先传递给该ViewGroup，之后才和down事件一样传递给最终的目标view的onTouchEvent()处理。

3.   如果该ViewGroup的onInterceptTouchEvent()在接收到down事件处理完成之后return true，那么后续的move, up等事件将不再传递给onInterceptTouchEvent()，而是和down事件一样传递给该ViewGroup的onTouchEvent()处理，注意，目标view将接收不到任何事件。

4.   如果最终需要处理事件的view的onTouchEvent()返回了false，那么该事件将被传递至其上一层次的view的onTouchEvent()处理。

5.   如果最终需要处理事件的view 的onTouchEvent()返回了true，那么后续事件将可以继续传递给该view的onTouchEvent()处理。
}

深圳 东经114°04′12〃 北纬22°37′12〃

1.service的IPC与回调时机与同步问题
{
	1.回调时机
	{
		 ANR 
		 1.client是UI线程调用server的话，server是当前线程处理回调，client才能响应UI线程的更新。

		 2.client是UI线程调用server的话，server是工作线程处理回调，client则不能响应UI线程的更新，
			否则报UI刷新线程不对异常。
		 
		 3.client是工作线程调用server的话，server无论是处于当前线程，还是另起子线程，
			client都不能处理UI线程的更新，只能处理数据。
		  
		 结合server的请求处理方式可以得出，通过回调可以实现异步通信的。
		 但是必须保证回调对象的生命周期与该对象所处的activity或者services
		 是同一个生命周期，即onCreate和onDestory，还有注册与注销的对称问题。
		 
		 当server没有任何client连接的时候，就会执行onUnbind和onDestory,只要有一个连接就不会执行该操作。
	}

	2.RPC时机
	{
		2.1 如果你在Service的onCreate或者onStart做一些很耗时间的事情，
			最好在 Service里启动一个线程来完成，因为Service是跑在主线程中，
			会影响到你的UI操作或者阻塞主线程中的其他事情。
		2.2 费时的RPC调用会阻塞UI线程，所以最好异步调用费时的RPC。
	}
}

3.优化Android性能的10种技巧
{

	技巧1：从优秀的编程开始

	要采用已为用户所接受的运算法则和标准的设计样式，这些被人们长期使用的编程法则也同样适用于Android应用，尤其当这些应用使用内在设备服务时。

	比如，假设你编写的应用需要以地理定位服务为基础。只需要在必要时开始注册进行位置更新，在无需更新信息时，确保应用停止更新进程。这会帮助节省设备的电量和系统处理器的负担。

	技巧2：保持应用的灵活性

	通过使用AsyncTask、IntentService或自定义背景服务来保持应用的灵活性。使用加载器来简化加载时间较长数据的状态管理，比如光标。不可让应用在其他进程进行时显得缓慢或完全静止。

	如果某些操作需要一定的时间和资源，应当将这个进程单独分离出来异步处理，这样你的应用才能够保持流畅的运行。可以运用这种方法的操作包括：磁盘读写，访问内容供应方、数据库和网络，其他需要较长时间的任务。

	技巧3：使用最新的Android SDK版本和API

	保持应用的更新，使用Android平台提供的最新内容。随着Android平台的发展，它也在逐步改善中。某些功能被移除，或者替换成更好的选项。其核心API中的漏洞已修复，整个API性能已得到提升。该平台已引入装载器之类的新API，帮助开发者编写更为稳定和反应灵敏的应用。

	Android 3.0应用支持硬件加速，你可以加以应用。应当理解的是，最佳的表现情况会随着时间逐渐改变。睿智的开发者会更新平台发布的最新内容和API。

	技巧4：检查Strict Mode

	你可以使用称为“StrictMode”的Android API来查找编程中的问题。StrictMode会帮助你识别应用是否正在耗费内存，也可以帮你检查应用是否正在尝试开展漫长的模块化操作。

	StrictMode类（注：即android.os.StrictMode）与Android 2.3同期发布。

	技巧5：在发布之前停用或最小化调试和诊断

	你在Android应用的开发中可能会将某些调试代码构建其中。在应用发布之前确保这些功能被最小化或完全停用。

	接下来，让我们来讨论如何用优秀的用户界面设计原则让你的应用加载速度更快。

	技巧6：保持布局简洁自然

	简洁自然的布局会加快加载速度。不要让屏幕布局中充斥过多不必要的内容。花点时间开发用户可以有效使用的简洁用户界面，不要将过多的功能性内容塞入单个屏幕中。这不仅对应用表现有帮助，而且会帮助用户更有效地使用应用。

	分割内容可以帮助划分用户界面功能性，同时不牺牲应用在各种不同设备上的灵活性。

	技巧7：根据目标设备调整应用资源

	根据特定的设备配置来调整资源，这样它们就能够有效地加载。在图像资源方面，这个显得尤为重要。如果你的应用中有大型的图片资源需要加载，那么要做好调整。

	另一个技巧是，当以许多种设备为目标时，保持应用包文件大小合适，只需要在其中包含应用运行所需的核心资源即可，然后让用户根据具体设备下载应用其他内容。

	技巧8：使用Hierarchy Viewer工具

	Hierarchy Viewer工具可以帮助你解除应用布局中的漏洞。它还提供了许多有价值的信息，比如每个View控制需要多长的时间。找到问题所属领域，这样解决问题会更加简单。

	技巧9：使用layoutopt工具

	layoutopt工具是个简单的命令行工具，可以帮助你识别不必要的控制和其他让你布局资源崩溃的事项，提升其性能。它可以帮助你找到不必要的多余布局控制。较少和较浅布局可优化应用运行性能。

	最后，在自认为应用达到最好状况时，对其进行测试。

	技巧10：使用Traceview和其他Android应用压缩工具

	Android SDK中有许多可以压缩应用的工具。可能最流行的工具就是Traceview，这个图像工具可以帮助你调试和找到应用的性能问题。

	结语

	目前有许多方法可以让你的Android应用运行加速。有些涉及到使用某种具体的运算法则，其他依靠某些真正的调试和运行监管技术。Android平台中有许多种免费的工具可以用来帮助跟踪和调整应用中的表现问题。你已经知道了以上10种技巧，现在可以尝试使用它们！
}


4.Intent的putExtra与getExtra时的对象必须是可序列化:Serializable
{
	否则传递对象的基础类型为null。
	其对象的成员对象最好全部是基础类型，非基础类型的也要是Serializable的成员对象。
	这在传递参数时，要注意！
}

5.如何成为“10倍效率”开发者
{
    1. 只做需要做的工作

    使用敏捷方法；
    全心全意做UX设计；
    沟通第一；
    编码也许不是解决问题的办法；
    过早的优化是一切罪恶的根源；
    选择最简单的解决方案

	2. 站在巨人的肩膀上

    使用开源框架；
    使用简洁语言（如HAML、Jade、Coffeescript）；
    不要做重复的事情（不要重新发明轮子）；
    利用包管理器来进行公共和私有代码分配；
    不要任凭巨头（如微软）的摆布而修复库中的一个Bug；
    不要让你的雇主逼你学习；
    自主学习并为自己设定新的目标。

	3. 了解数据结构和算法

	如果你不知道什么时候应该使用快速排序、不懂辨认O(n2)程序、不会写递归函数，
	你将无法成为10倍效率的开发者。使用多种语言你才能清楚不同的框架是如何解决相同问题的。
    尽可能去了解底层命令（plumbing），以便能够作出明智的决定
   （Web框架是怎么存储session状态的？Cookie到底是什么？）。

	4. 不要怕买工具，它可以节省你的时间
	Ben说：“昨天我花50美元买了一个位图字体工具，它帮我节省的时间成本绝对超过200元。”

	5. 集中注意力
	不要整天开着你的电子邮件、Twitter、Facebook等，在工作时将它们最小化或关掉它们，
	戴上耳机。Tiny hack说：“即使不听音乐我也戴着耳机工作，这样便不会有人打扰到我。”

	6. 尽早并且经常性地进行代码重构
	有时，你不得不放弃漂亮的代码转而去寻找真正对项目有用的代码，但没关系，
	如果你的现有项目中有这样的代码，最好的方式便是不要看它，并重构。

	7. 只管去做
	将你的业余项目分享到Startup Weekend中。在我开始转到Unix和Ruby on Rails上之前，
	我买了一台Mac，使用Windows虚拟机花了一年时间做.NET项目。

	8. 挑选一个编辑器，并掌握它
	高效开发者喜欢用文本编辑器胜过IDE编辑器，因为这样可以学到更多东西。无论什么情况，
	尽量使用键盘快捷键，因为熟练使用一件工具的前提是熟悉它。
	在选择编辑器时，认真考虑并挑选最好的（Emacs或Vim），因为它们是通用的。其次，
	挑选你的首选平台最支持的。使用宏，不断地写代码；使用 Mac上的TextExpander为
	整个段落创建快捷方式；使用Visual Studio或SublimeText的自动补齐功能；
	使用支持按行/列分割窗口的编辑器，这样你便能同时看到单元测试和代码（或模型、视图）。
	一定要想清楚后再写代码。Adam说，“我有朋友在一个大项目组里工作，他们组里最高效的
	程序员是一个高位截瘫用嘴叼着棍子敲代码的人，他总是在写代码之前想得很仔细且很少出错。”

	9. 整洁的代码胜过巧妙的代码
	要想让其他人能够读懂你的代码，尽量使用最少的代码来完成任务。遵循DRY（Don't repeat yourself）
	的原则，使用明确定义的对象和库，将任务分解成小而简单的代码段。

	10. 潜意识是强大的工具
	离开10分钟往往就可以解决一个问题。控制编程时间，给自己一个多姿多彩的生活，
	劳逸结合能让你在工作时更高效、更愉悦。当然，即便是上了年纪的程序员也知道，
	以最少的时间完成最高效的工作是成为10倍效率开发者的必要条件。
	作为一个程序员，我觉得在职业生涯中最好的一件事儿就是从电脑前站起来，
	去拜访那些在某一领域有所建树的人们。

	11. 推动自身和团队进步
	重视批评，以包容的态度接受批评并提升自己是非常重要的事情。没有这个基础，
	你不可能成为一个高效的开发者。
	一位智者曾经说过：“聪明的人善于从自己的错误中学习，而智慧的人善于从别人的错误中学习。”
}

1.Android Paint的介绍
{
  
     * Paint类介绍  
     *   
     * Paint即画笔，在绘图过程中起到了极其重要的作用，画笔主要保存了颜色，  
     * 样式等绘制信息，指定了如何绘制文本和图形，画笔对象有很多设置方法，  
     * 大体上可以分为两类，一类与图形绘制相关，一类与文本绘制相关。         
     *   
     * 1.图形绘制  
     * setARGB(int a,int r,int g,int b);  
     * 设置绘制的颜色，a代表透明度，r，g，b代表颜色值。  
     *   
     * setAlpha(int a);  
     * 设置绘制图形的透明度。  
     *   
     * setColor(int color);  
     * 设置绘制的颜色，使用颜色值来表示，该颜色值包括透明度和RGB颜色。  
     *   
    * setAntiAlias(boolean aa);  
     * 设置是否使用抗锯齿功能，会消耗较大资源，绘制图形速度会变慢。  
     *   
     * setDither(boolean dither);  
     * 设定是否使用图像抖动处理，会使绘制出来的图片颜色更加平滑和饱满，图像更加清晰  
     *   
     * setFilterBitmap(boolean filter);  
     * 如果该项设置为true，则图像在动画进行中会滤掉对Bitmap图像的优化操作，加快显示  
     * 速度，本设置项依赖于dither和xfermode的设置  
     *   
     * setMaskFilter(MaskFilter maskfilter);  
     * 设置MaskFilter，可以用不同的MaskFilter实现滤镜的效果，如滤化，立体等       *   
     * setColorFilter(ColorFilter colorfilter);  
     * 设置颜色过滤器，可以在绘制颜色时实现不用颜色的变换效果  
     *   
     * setPathEffect(PathEffect effect);  
     * 设置绘制路径的效果，如点画线等  
     *   
     * setShader(Shader shader);  
     * 设置图像效果，使用Shader可以绘制出各种渐变效果  
     *  
     * setShadowLayer(float radius ,float dx,float dy,int color);  
     * 在图形下面设置阴影层，产生阴影效果，radius为阴影的角度，dx和dy为阴影在x轴和y轴上的距离，color为阴影的颜色  
     *   
     * setStyle(Paint.Style style);  
     * 设置画笔的样式，为FILL，FILL_OR_STROKE，或STROKE  
     *   
     * setStrokeCap(Paint.Cap cap);  
     * 当画笔样式为STROKE或FILL_OR_STROKE时，设置笔刷的图形样式，如圆形样式  
     * Cap.ROUND,或方形样式Cap.SQUARE  
     *   
     * setSrokeJoin(Paint.Join join);  
     * 设置绘制时各图形的结合方式，如平滑效果等  
     *   
     * setStrokeWidth(float width);  
     * 当画笔样式为STROKE或FILL_OR_STROKE时，设置笔刷的粗细度  
     *   
     * setXfermode(Xfermode xfermode);  
     * 设置图形重叠时的处理方式，如合并，取交集或并集，经常用来制作橡皮的擦除效果  
     *   
     * 2.文本绘制  
     * setFakeBoldText(boolean fakeBoldText);  
     * 模拟实现粗体文字，设置在小字体上效果会非常差  
     *   
     * setSubpixelText(boolean subpixelText);  
     * 设置该项为true，将有助于文本在LCD屏幕上的显示效果  
     *   
     * setTextAlign(Paint.Align align);  
     * 设置绘制文字的对齐方向  
     *   
   * setTextScaleX(float scaleX);  
    * 设置绘制文字x轴的缩放比例，可以实现文字的拉伸的效果  
     *   
     * setTextSize(float textSize);  
     * 设置绘制文字的字号大小  
     *   
     * setTextSkewX(float skewX);  
     * 设置斜体文字，skewX为倾斜弧度  
     *   
     * setTypeface(Typeface typeface);  
     * 设置Typeface对象，即字体风格，包括粗体，斜体以及衬线体，非衬线体等  
     *   
     * setUnderlineText(boolean underlineText);  
     * 设置带有下划线的文字效果  
     *   
     * setStrikeThruText(boolean strikeThruText);  
     * 设置带有删除线的效果  
     *   
	 } 
1.大话设计模式 读书笔记：6个原则
{
	 单一职责原则：就一个类而言，应该仅有一个引起它变化的原因。
如果一个类承担的职责过多，就等于把这些职责耦合在一起，一个职责的变化可能会削弱或者抑制这个类完成其他职责的能力。这个耦合会导致脆弱的设计，当变化发生时，设计会遭到意想不到的破坏。

软件设计真正要做的许多内容，就是发现职责并把那些职责相互分离；如果能够想到多于一个动机去改变一个类，那么这个类就具有多于一个职责，就应该考虑类的职责分离。

 

开放-封闭原则：软件实体（类、模块、函数/方法等等）应该可以扩展，但是不可修改。
对于扩展是开放的，而对于更改是封闭的。

开放-封闭原则的作用是使软件的设计能够面对需求的改变却可以保持相对的稳定，从而使得系统可以在第一个版本以后不断推出新的版本。

绝对的对修改封闭是不可能的。无论模块多么“封闭”，都会存在一些无法对之封闭的变化。既然不可能完全封闭，设计人员必须对于他设计的模块应该对哪种变化封闭做出选择。必须先猜测出最有可能的发生的变化种类，然后构造抽象来隔离那些变化。

在最初编写代码时，假设变化不会发生。等到变化发生时立即采取行动，创建抽象来隔离以后发生的同类变化。

面对需求，对程序的改动是通过增加新的代码进行的，而不是更改现有代码。

在开发工作展开不久就应该知道可能发生的变化，查明可能发生的变化所等待的时间越长，要创建正确的抽象就越困难。

开放-封闭原则是面向对象设计的核心所在。遵循这个原则可以带来面向对象技术所声称的巨大好处，也就是可维护、可扩展、可复用、灵活性好。开发人员应该对程序中呈现出频繁变化的那些部分作出抽象。然而，对于应用程序中的每个部分都刻意进行抽象同样不是好主意。拒绝不成熟的抽象和抽象本身一样重要。

 

里氏代换原则：子类型必须能够替换掉它们的父类型。
一个软件实体如果使用的是一个父类的话，那么一定适用于其子类，而且它察觉不出父类对象和子类对象的区别。也就是说，在软件中，把父类都替换成它的子类，程序的行为没有变化。

只有当子类可以替换掉父类，软件单位的功能不受到影响时，父类才能真正被复用，而子类也才能够在父类的基础上增加新的行为。

由于子类型的可替代性才使得使用父类型的模块在无需修改的情况下就能够扩展。

 

依赖倒转原则：
A．高层模块不应该依赖低层模块。二者都应该依赖抽象。
B． 抽象不应该依赖细节。细节应该依赖抽象。
针对接口编程，不要针对实现编程。

面向过程开发时，为了使得常用代码可以复用，一般都会把这些代码常用代码写成许多函数的程序库，这样在做新项目时，去调用这些低层的函数就可以了。这就叫做高层模块依赖低层模块。依赖倒转原则就是不管高层模块还是低层模块，都依赖于抽象，具体一点就是接口或抽象类，只要接口是稳定的，那么任何一个的更改都不用担心其他受到影响，这就使得无论高层模块还是低层模块都可以很容易被复用。

依赖倒转其实可以说是面向对象设计的标志，用哪种语言来编写程序不重要，如果编写时考虑的都是如何针对抽象编程而不是针对细节编程，即程序中所有的依赖关系都是终止于抽象类或者接口，那就是面向对象的设计，反之那就是过程化的设计。

 

迪米特法则（最少知识原则）：如果两个类不必彼此直接通信，那么这两个类就不应当发生直接的相互作用。如果其中一个类需要调用另一个类的某一个方法的话，可以通过第三者转发这个调用。
迪米特法则首先强调的前提是在类的结构设计上，每一个类都应当尽量降低成员的访问权限。其根本思想是强调了类之间的松耦合。类之间的耦合越弱，越有利于复用，一个处在弱耦合的类被修改，不会对有关系的类造成波及。

 

合成/聚合复用原则：尽量使用合成和聚合，尽量不要使用类继承。
聚合表示一种弱的“拥有”关系，体现的是A对象可以包含B对象，但B对象不是A对象的一部分；合成则是一种强的“拥有”关系，体现了严格的部分和整体的关系，部分和整体的生命周期一样。

优先使用对象的合成/聚合将有助于保持每个类的封装，并被集中在单个任务上。这样类和类继承层次会保持较小的规模，并且不太可能增长到不可控制的庞然大物。
}

1.好的项目工具
{
	Hypertable(C++)
	Hadoop
	protobuf()
	netty(java network lib)
	KryoNet
	folly(facebook c++ lib)
	udptunnel(tcp data in udp)
	viewflow-https://nodeload.github.com/pakerfeldt/android-viewflow/zipball/master
	memcached
	node.js
	Tokyo Tyrant
	Tokyo Cabinet
	redis
	GO
	MQTT
	XMPP
	libevent
	libev
	mysql
	Nginx
	BoneCp
	Guzz
	dk_server
	mcd_server	
	UDT
	ENet
	Barefoot
	WiEngine
	cocos2d-x
	WebSocket
	socket.io
	jWebSocket
	CshBBrain
	Drupal是一个开源的内容管理系统(CMS) 平台
}

1.集合
{
	http://linux.linuxidc.com/
	高性能高并发服务器架构文章大全.doc
	http://gitorious.org
	github
	http://developer.android.com/training
	oschina
}
2.项目总结
{
	AIzaSyDOVYLtZPtkMuiY_sNfUTh4o6yBLo76BrE
	0vpN4dS02c7thnCLT4C6UyA_3zvWXiMeWsambiA[zdh_keystore]
	0vpN4dS02c7tI5xVafN8cnDb-lg_rS8ET528Hsg[debug_keystore]
	rj6tp7RX4Hw6
}
4.云服务
{
    SAE: 新浪云服务平台
    
}

3.10个步骤让你成为高效的Web开发者
{
1. 使用Web开发框架
一个良好的Web开发框架，可以帮助你

解决常见的Web开发问题，如标准的Web页面布局、Web表单处理、浏览器兼容性解决等）。
测试Web开发的bug、安全和性能。
使代码重用更容易，因为有一个统一的API。
社区开发的插件和工具，可以进一步增强开发（jQuery有很多非常棒的插件，比如用户界面、图像和网页排版等）。
遵循编码的最佳实践和伟大的编程设计模式，你甚至不用去思考。
有开发社区和用户的支持，你可以获取帮助以及进行协作。
初学者使用起来非常容易。
有很多Web开发框架你可以选择

完整的Web应用程序框架，如Ruby on Rails或CakePHP。
JavaScript框架，如jQuery、MooTools和其他许多鲜为人知但很优秀的用于处理用户界面、客户端逻辑和Ajax的JavaScript框架。
如果你想加快视觉层设计，你可以使用一个CSS框架（如，Toast），或者网页布局框架（如，960 Grid System）。
专门的框架，如用于内容管理系统开发的WordPress主题框架。
现在还有移动Web应用程序开发框架。

2. 建立一个代码片段库

IT界的名言“不要重复发明轮子”。回收你的代码，创建一个你最喜欢和经常使用的代码片段集，并确保代码组织良好，可以被轻易搜索到。

最简单的方法是，你可以在硬盘中建立源代码目录。但这未必是最好的方式。

一种方法是使用允许保存代码片段的源代码编辑器或IDE。例如，Notepad++的SnippetPlus插件和Dreamweaver内置的片段面板。
Adobe Dreamweaver的片段面板

另一种方法就是使用专门的片段工具，如Snippet或Snipplr.。

你甚至可以使用自己喜欢的代码库/版本控制系统，像Git，来更好地管理你的源代码。

3. 使用正确的工具

这应该是显而易见的，确保你拥有一些优秀的、你知道如何更有效地使用的工具和装备。

如果你是一个网页设计师，你可能需要Photoshop和Illustrator。如果你是一名开发人员，你需要一些优秀的Web开发应用程序，以帮助你完成工作。

当然，各领域之间、开发者之间的工具集是不同的。重要的是你的工具应该可以帮助你尽可能地高效完成工作。

4. 收集和整理免费设计资源

在网络上有许多网站放出免费设计资源。你可以将这些网站添加到你的RSS阅读器，它们每发布一个免费软件或资源，你就可能了解到。

对于一个设计师来说，这样可以得到大量的图标、PSD模板、矢量插图、风景图像、按钮、GUI元素、Photoshop笔刷、字体库和各种其他的设计元素。

5. 冲刺式工作（短时间高效工作）

不要连续工作几个小时，这样你的效率可能会下降，随着时间的推移，你的工作品质也会下降。每次连续工作10~20分钟，可以保持你大脑活跃，可以更好地产生新的想法。

在短时间的工作，意味着你总是有一个新鲜的开始。

6. 休息

冲刺工作最重要的部分是休息。让你的头脑休息至关重要。利用这些休息时间将自己的工作区分开，干些其他工作或运动一下。

另外，除非有必要，否则不要计划你的时间。你不必每次都工作15分钟，然后休息5分钟。要选一个好的休息点，有一个舒适的休息。

你可以参考番茄工作法。

7. 在学习上投入时间

短期内，不断学习、跟上行业形势并保持领先是很费时间的。

然而，通过在自我提高上投入时间，你可以在工作的其他方面节省时间，可以增加你的劳动成果并提高工作质量。

8. 不要过度规划

你不应该严格规划你的每一天。你的头脑需要有一定的灵活性，需要有时间去认真思考。

请一定要保持你的时间表灵活变化。我发现，超过规划的工作，我可能会一拖再拖。

9. 不要急于直接开始

这和过度规划是两个极端。虽然你不应该过度规划，但你需要计划下。

每当你开始一个项目的工作，确保知道你对这个项目的期望。做网页设计项目时，需要了解客户期望做的、首页内容、颜色设置等工作。

了解有关项目的某些内容，可能会使你工作在正确的方向，而不至于在黑暗中摸索。

在我开始客户的网站项目工作时，我需要先确定：

颜色方案
网站的目标
他们能够提供的任何内容
他们已经拥有的任何设计思路

10. 不要使事情复杂化

这可能是所有这些最重要的一条：不要使你的工作复杂化。不要做更多的工作，不要在给定的时间内为自己分配过多的工作。我不主张给你的客户提供最少的交付时间，但你肯定也不能提供最长的时间。 要明白客户他们需要什么，不能多也不能少。 另外，不要制定过于复杂的开发战略。让事情简单一些。
}

4.Web前端：11个让你代码整洁的原则
{
1.DOCTYPE的声明

如果我们想做好一件事情，首先要知道我们有哪些权利去做，就如“DOCTYPE”的声明，我们没有必要去讨论是否使用HTML4.01或者XHTML1.0或者说现在的HTML5都提供了严格版本或者过渡版本，这些都能很好的支持我们写的代码：



由于我们现在的布局不需要table布局也能做出很好的布局，那么我们就可以考虑不使用过渡型而使用严格型的“DOCTYPE”，为了向后兼容，我建议使用HTML5的声明模式：

<!DOCTYPE HTML>

<html lang="en-US">

如果想了解更多这方面的知识，可以点击：

W3C: Recommended DTDs to use in your Web document-->http://www.w3.org/QA/2002/04/valid-dtd-list.html
Fix Your Site With the Right DOCTYPE!-->http://www.alistapart.com/stories/doctype/
No more Transitional DOCTYPEs， please-->http://www.456bereastreet.com/archive/200609/no_more_transitional_doctypes_please/
2.字符集和编码字符

在每个页面的开始中，我们都在<head>中设置了字符集，我们这里都是使用“UTF-8”

<meta charset="UTF-8" /> 
而且我们在平时写页面中时，时常会碰到"&"这样的符号，那么我们不应该直接在页面这样写“&”:



 我们应该在代码中使用字符编码来实现，比如说“&”我们在代码中应该使用“&amp;”来代替他。

如果想了解更多这方面的知识，可以点击：

Wikipedia: UTF-8
A tutorial on character code issues-->http://www.cs.tut.fi/~jkorpela/chars.html
The Extended ASCII table-->http://www.ascii-code.com/

3.正确的代码缩进

在页面编辑中，代码的缩进有没有正确，他不会影响你网站的任何功能，但要是你没有一个规范的缩进原则，让读你代码的人是非常的生气，所以说正确的代码缩进可以增强你的代码可读性。标准程序的缩进应该是一个制表符（或几个空格），形像一点的我们来看下文章开头那张图，或者一起来看下面展示的这张图，你看后就知道以后自己的代码要怎么样书写才让人看了爽：



不用说，大家都喜欢下面的那种代码吧。这只是一个人的习惯问题，不过建议从开始做好，利人利已。
有关于这方面的介绍，
	大家还可以参考：Clean up your Web pages with HTML TIDY。-->http://www.w3.org/People/Raggett/tidy/

4.外链你的CSS样式和Javascript脚本

页面中写入CSS样式有很多种方法，有些直接将样式放入页面的“<head>”中，这将是一个很不好的习惯，因为这样不仅会搞乱我们的标记，而且这些样式只适合这一个HTML页面。所以我们需要将CSS单独提出，保存在外部，这样后面的页面也可以链接到这些样式，如果你页面需要修改，我们也只需要修改样式文件就可以。正如下图所示：
上面我们所说的只是样式，其实javascript脚本也和CSS样式是同一样的道理。图文并说，我最终想表达的意思是“在制作web页面中，尽量将你的CSS样式和javascript脚本单独放在一个文件中，然后通过链接的方式引用这些文件，这样做的最大好处是，方便你的样式和脚本的管理与修改。”

5.正确的标签嵌套

我们在写HTML时总是需要标签的层级嵌套来帮我们完成HTML的书写，但这些HTML的嵌套是有一定的规则的，如果要细说的话，我们可能要用几个章节来描述，那么我今天这里要说的是，我们在写HTML时不应该犯以下这样的超级错误：
上图的结构我们是常见的，比如说首页的标题，那么我们就应该注意了，不能把“<h1>”放在“<a>”标签中，换句话说，就是不能么块元素和在行内元素中。上面只是一个例子，只是希望大家在平时的制作中不应该犯这样的超级错误。

6.删除不必要的标签

首先我们一起来看一个实例的截图：



上图明显是一个导航菜单的制作，在上图的实例中：有一个“div#topNav”包住了列表“ul#bigBarNavigation”，而“div”和“ul”列表都是块元素，加上“div”此处用来包“ul”根本就没有起到任何作用。虽然“div”的出现给我们制作web页面带来了极大的好处，但我们也没有必要到处这样的乱用，不知道大家平时有没有注意这样的细节呢？我是犯这样的错误，如果你也有过这样的经历，那么从今天开始，从现在开始，我们一起努力来改正这样的错误。

有关于如何正确的使用标签，
	大家感兴趣的话可以点击：Divitis: what it is, and how to cure it. -->http://csscreator.com/?q=divitis

7.使用更好的命名

这里所说的命名就是给你的页面中相关元素定义类名或者是ID名，很多同学都有这栗的习惯，比如说有一个元素字体是红色的，给他加上“red”,甚至布局都写“left-sidebar”等，但是你有没有想过，如果这个元素定义了“red”后，过几天客户要求使用“蓝色”呢？或者又说，那时的“left-sidebar”边栏此时不想放在左边了，而是想放在右边，那么这样一来我们前面的命名可以说是一点意义都没有了，正如下面的一个图所示：



那么定义一个好的名就很得要了，不但自己能看懂你的代码，而且别人也能轻松读懂你的代码，比如说一个好的类名和ID名“mainNav”、“subNav”、“footer”等，他能描述所包含的事情。不好的呢，比如前面所说的。

如果想了解更多这方面的知识，可以点击：

Standardizing CSS class and id names -->http://www.techrepublic.com/article/standardizing-css-class-and-id-names/5286783
CSS Tip #2: Structural Naming Convention in CSS-->http://sixrevisions.com/css/css-tips/css-tip-2-structural-naming-convention-in-css/
CSS coding: semantic approach in naming convention-->http://woork.blogspot.com/2008/11/css-coding-semantic-approach-in-naming.html
CSS Naming Conventions and Coding Style-->http://www.realdealmarketing.net/docs/css-coding-style.php

8.离开版本的CSS

我们在设计菜单时，有时要求所有菜单选项的文本全部大写，大家平时是不是直接在HTML标签中就将他们设置成大写状态呢？如果是的话，我觉得不好，如果为了将来具有更好的扩展性，我们不应该在HTML就将他们设置为全部大写，更好的解决方法是通过CSS来实现：

9.定义<body>的类名或ID名

大家平时制作web页面时不知道有没有碰到这样的问题，就是整站下来，使用了相同的布局和结构，换句话说，你在页面的布局上使用了相同的结构，相同的类名，但是突然你的上级主管说应客户的需求，有一个页面的布局需要边栏和主内容对换一下。此时你又不想为了改变一下布局而修改整个页面的结构，此时有一个很好的解决办法，就是在你的这个页面中的“<body>”中定义一个特殊的类名或ID名，这样来你就可以轻松的达到你所要的需求。这样的使用，不知道大家使用过没有：



给“<body>”定义独特的类和ID名称是非常强大的，不仅仅是为了像上面一样帮你改变布局，最主要的是有时他能帮你实现页面中的某一部分达到特殊效果，而又不影响其它页面的效果。为什么有这样的功能，不用我说我想大家都是知道的。因为每个页面的内容都是“<body>”的后代元素。

如果想了解更多这方面的知识，可以点击：

ID Your Body For Greater CSS Control and Specificity-->http://css-tricks.com/id-your-body-for-greater-css-control-and-specificity/
Case study: Re-using styles with a body class-->http://www.37signals.com/svn/archives2/case_study_reusing_styles_with_a_body_class.php

10.验证你的代码

人不免会出错，我们编写代码的时候也是一样的，你有时候总会小写或多写，比如说忘了关闭你的元素标签，不记得写上元素必须的属性，虽然有一些错误不会给你带来什么灾难性的后果，但也不免会给你带来你无法意估的错误。所以建议您写完代码的时候去验证你一下你的代码。验证后的代码总是比不验证的代码强：



为一有效的验证你的代码，我们可以使用相关的工具或者浏览器的插件来帮助我们完成。如果你的代码没有任何错误，W3C验证工具会在你们面前呈现绿色的文字，这样让你是无比的激动人心，因为再次证明了你写的代码经得起W3Ｃ的标准。

如果想了解更多这方面的知识，可以点击：

The W3C Markup Validation Service-->http://validator.w3.org/
XHTML-CSS Validator-->http://xhtml-css.com/
Free Site Validator (checks entire site, not just one page)-->http://freesitevalidator.com/

11.逻辑顺序

这是一个很少见的错误情况，因为我想大家写页面都不会把逻辑顺序打乱，换句话说，如果可能的话，让你的网站具有一个先后逻辑顺序是最好的，比如说先写页头，在写页体，最后写页脚。当然有时也会碰到特殊情况，如何页脚部分在于我们代码的边栏以上，这可能是因为它最适合你的网站设计需求，这样或许是可以理解的，但是如果你有别的方式实现，我们都应该把页脚是放在一个页面的最后，然后在通过特定的技术让它达到你的设计需求：

上面我们一起讨论了多个如何让你开始写一个整洁的HTML代码。从一个项目的开始，这一切都是非常容易的，但是如果需要你去修复一个现有的代码，那多少都会有一定的难度。上面说这么多主要是告诉您将要如何学习编写一个良好的、整洁的HTML代码，并且一直坚持这样的编写。希望读完这篇文章后，在你的下一个项目中，能从头开始，坚持写一个整洁的HTML代码。
}

0.值得学习！Google的编程样式指南
{
C语言样式指南；http://google-styleguide.googlecode.com/svn/trunk/google-c-style.el
C++样式指南；http://google-styleguide.googlecode.com/svn/trunk/cppguide.xml
Objective-C样式指南；http://google-styleguide.googlecode.com/svn/trunk/objcguide.xml
Python样式指南；http://google-styleguide.googlecode.com/svn/trunk/pyguide.html
HTML/CSS样式指南；http://google-styleguide.googlecode.com/svn/trunk/htmlcssguide.xml
JavaScript样式指南；http://google-styleguide.googlecode.com/svn/trunk/javascriptguide.xml
XML样式指南；http://google-styleguide.googlecode.com/svn/trunk/xmlstyle.html
R语言样式指南；http://google-styleguide.googlecode.com/svn/trunk/google-r-style.html
cpplint样式指南：http://google-styleguide.googlecode.com/svn/trunk/cpplint
}

1.android 面试问题
{
	1.Android中SurfaceView和View的区别
	{
	SurfaceView和View最本质的区别在于，surfaceView是在一个新起的单独线程中可以重新绘制画面
	而View必须在UI的主线程中更新画面。那么在UI的主线程中更新画面 可能会引发问题，比如
	你更新画面的时间过长，那么你的主UI线程会被你正在画的函数阻塞。那么将无法响应按键，
	触屏等消息。当使用surfaceView 由于是在新的线程中更新画面所以不会阻塞你的UI主线程。
	但这也带来了另外一个问题，就是事件同步。比如你触屏了一下，你需要surfaceView中thread处理，
	一般就需要有一个event queue的设计来保存touch event，这会稍稍复杂一点，因为涉及到线程同步。 

　　所以基于以上，根据游戏特点，一般分成两类:
　　1.被动更新画面的。比如棋类，这种用view就好了。因为画面的更新是依赖于 onTouch 来更新，
	可以直接使用 invalidate。 因为这种情况下，这一次Touch和下一次的Touch需要的时间比较长些，
	不会产生影响。 

　　2.主动更新。比如一个人在一直跑动。这就需要一个单独的thread不停的重绘人的状态，
	避免阻塞main UI thread。所以显然view不合适，需要surfaceView来控制。
	}

	2.Android 中View的更新介绍——多线程和双缓冲
	{
	1.不使用多线程和双缓冲
     这种情况最简单了，一般只是希望在View发生改变时对UI进行重绘。你只需在Activity中
		显式地调用View对象中的invalidate()方法即可。系统会自动调用 View的onDraw()方法。
	2.使用多线程和不使用双缓冲
     这种情况需要开启新的线程，新开的线程就不好访问View对象了。强行访问的话会报：
	 android.view.ViewRoot$CalledFromWrongThreadException：Only the original thread that created a view hierarchy can touch its views.
     这时候你需要创建一个继承了android.os.Handler的子类，并重写handleMessage(Message msg)方法。android.os.Handler是能发送和处理消息的，
	 你需要在Activity中发出更新UI的消息，然后再你的Handler（可以使用匿名内部类）中处理消息
	（因为匿名内部类可以访问父类变量， 你可以直接调用View对象中的invalidate()方法 ）。
     也就是说：在新线程创建并发送一个Message，然后再主线程中捕获、处理该消息。
	3.使用多线程和双缓冲
	  Android中SurfaceView是View的子类，她同时也实现了双缓冲。你可以定义一个她的子类并
		实现SurfaceHolder.Callback接口。由于实现SurfaceHolder.Callback接口，新线程就
		不需要android.os.Handler帮忙了。SurfaceHolder中lockCanvas()方法可以锁定画布，
		绘制玩新的图像后调用unlockCanvasAndPost(canvas)解锁（显示），还是比较方便得。
	}

	3.hashcode 与 equals（）区别？
	{
		1、默认情况（没有覆盖equals方法）下equals方法都是调用Object类的equals方法，
			而Object的equals方法主要用于判断对象的内存地址引用是不是同一个地址（是不是同一个对象）。

		2 、要是类中覆盖了equals方法，那么就要根据具体的代码来确定equals方法的作用了，
			覆盖后一般都是通过对象的内容是否相等来判断对象是否相等。

		3、hashcode方法只有在集合中用到.

		4、将对象放入到集合中时，首先判断要放入对象的hashcode值与集合中的任意一个元素的
			hashcode值是否相等，如果不相等直接将该对象放入集合中。如果hashcode值相等，
			然后再通过equals方法判断要放入对象与集合中的任意一个对象是否相等，如果equals判断不相等，
			直接将该元素放入到集合中，否则不放入.
	}

	4.dp,px,sp区别及使用场景
	{
		1.px (pixels)（像素）：屏幕上的点 ，与密度相关。密度大了，单位面积上的px会比较多。 
		2.dip或dp（与密度无关的像素）。这个和设备硬件有关，为了支持WVGA、HVGA和QVGA 5进制空间推荐使用这个。一种基于屏幕密度的抽象单位。设置一些view的宽高可以用这个，一般情况下，在不同分辨率，都不会有缩放的感觉。如果用px的话，320px占满HVGA的宽度，到WVGA上就只能占一半不到的屏幕了，那一定不是你想要的。 
		3.sp（与刻度无关的像素）放大像素– 主要处理字体的大小。

		但是，需要注意的是，在一个低密度的小屏手机上，仅靠上面的代码是不能获取正确的尺寸的。比如说，一部240x320像素的低密度手机，如果运行上述代码，获取到的屏幕尺寸是320x427。因此，研究之后发现，若没有设定多分辨率支持的话，Android系统会将240x320的低密度（120）尺寸转换为中等密度（160）对应的尺寸，这样的话就大大影响了程序的编码。所以，需要在工程的AndroidManifest.xml文件中，加入supports-screens节点，具体的内容如下：
        <supports-screens
            android:smallScreens="true"
            android:normalScreens="true"
            android:largeScreens="true"
            android:resizeable="true"
            android:anyDensity="true" />
		这样的话，当前的Android程序就支持了多种分辨率，那么就可以得到正确的物理尺寸了。
	}

	5.c++引用与指针的区别（着重理解）
	{
	★ 相同点：

    1. 都是地址的概念；
    指针指向一块内存，它的内容是所指内存的地址；引用是某块内存的别名。

    ★ 区别：

    1. 指针是一个实体，而引用仅是个别名；

    2. 引用使用时无需解引用（*），指针需要解引用；

    3. 引用只能在定义时被初始化一次，之后不可变；指针可变；

    引用“从一而终” ^_^

    4. 引用没有 const，指针有 const，const 的指针不可变；

    5. 引用不能为空，指针可以为空；

    6. “sizeof 引用”得到的是所指向的变量（对象）的大小，而“sizeof 指针”得到的是指针本身（所指向的变量或对象的地址）的大小；

    typeid（T） == typeid（T&） 恒为真，sizeof（T） == sizeof（T&） 恒为真，但是当引用作为成员时，其占用空间与指针相同（没找到标准的规定）。

    7. 指针和引用的自增（++）运算意义不一样；

    ★ 联系

    1. 引用在语言内部用指针实现（如何实现？）。

    2. 对一般应用而言，把引用理解为指针，不会犯严重语义错误。引用是操作受限了的指针（仅容许取内容操作）。
	}

	2.怎样使一个Android应用不被杀死？
	{
	完全让进程不被kill是不可能的，我们可以通过一些操作，使进程被kill的几率变小：
	1) 提高进程的优先级:
        * 后台操作采用运行于前台的Service形式，因为一个运行着service的进程比
		一个运行着后台activity的等级高；
        * 按back键使得进程中的activity在后台运行而不是destory，
		需重载back按键(没有任何activity在运行的进程优先被杀).
        * 依赖于其他优先级高的进程；
	2) 强制修改进程属性：
        * 在进程中设置：setPersistent(true);
        * 在Manifest文件中设置（如上）。
	}

	3.BroadcastReceiver的生命周期
	{
		onReceiver调用结束，生命周期结束。
	}

	4.Requestlayout，onlayout，onDraw，DrawChild区别与联系
	{
		RootView 只有一个孩子就是 DecorView，这里整个 View Tree 都是 DecorView 的子 View。
		整个View树的绘图流程：
			流程一： mesarue()过程	  --> onMesarue()
			 主要作用：为整个View树计算实际的大小，即设置实际的高(对应属性:mMeasuredHeight)和宽(对应属性:
			mMeasureWidth)，每个View的控件的实际宽高都是由父视图和本身视图决定的。
			
			流程二、 layout布局过程： --> onLayout()
			主要作用 ：为将整个根据子视图的大小以及布局参数将View树放到合适的位置上。

			流程三、 draw()绘图过程   --> onDraw() -->dispatchDraw()
			由ViewRoot对象的performTraversals()方法调用draw()方法发起绘制该View树，值得注意的是每次发起绘图时，并不
			会重新绘制每个View树的视图，而只会重新绘制那些“需要重绘”的视图，View类内部变量包含了一个标志位DRAWN，当该
			视图需要重绘时，就会为该View添加该标志位。

			强调一点的就是，在这三个流程中，Google已经帮我们把draw()过程框架已经写好了，自定义的ViewGroup只需要实现
			measure()过程和layout()过程即可 。

			这三种情况，最终会直接或间接调用到三个函数，分别为invalidate()，requsetLaytout()以及requestFocus() ，接着
			这三个函数最终会调用到ViewRoot中的schedulTraversale()方法，该函数然后发起一个异步消息，消息处理中调用
			performTraverser()方法对整个View进行遍历。

			1.invalidate()方法 ：
			{
			说明：请求重绘View树，即draw()过程，假如视图发生大小没有变化就不会调用layout()过程，并且只绘制那些“需要重绘的”
				视图，即谁(View的话，只绘制该View ；ViewGroup，则绘制整个ViewGroup
				dispatchDraw()-->drawChild()
				)请求invalidate()方法，就绘制该视图。
 
			一般引起invalidate()操作的函数如下：
            1、直接调用invalidate()方法，请求重新draw()，但只会绘制调用者本身。
            2、setSelection()方法 ：请求重新draw()，但只会绘制调用者本身。
            3、setVisibility()方法 ： 当View可视状态在INVISIBLE转换VISIBLE时，会间接调用invalidate()方法，
                     继而绘制该View。
            4、setEnabled()方法 ： 请求重新draw()，但不会重新绘制任何视图包括该调用者本身。
			}

			2.requestLayout()方法 ：
			{
			会导致调用measure()过程 和 layout()过程 。
 
			说明：只是对View树重新布局layout过程包括measure()和layout()过程，不会调用draw()过程，但不会重新绘制
			任何视图包括该调用者本身。
 
			一般引起invalidate()操作的函数如下：
			1、setVisibility()方法：
             当View的可视状态在INVISIBLE/ VISIBLE 转换为GONE状态时，会间接调用requestLayout() 和invalidate方法。
			同时，由于整个个View树大小发生了变化，会请求measure()过程以及draw()过程，同样地，只绘制需要“重新绘制”的视图。
			}
	}

	5.Android 平台提供了两类动画
	{
		一类是 Tween 动画，即通过对场景里的对象不断做图像变换 ( 平移、缩放、旋转、透明 ) 产生动画效果；
		第二类是 Frame 动画，即顺序播放事先做好的图像，跟电影类似。

		rootView[DecorView[ParentView[ChildVie,ChildView]]]
		Android 动画就是通过 ParentView 来不断调整 ChildView 的画布坐标系来实现的，
	    下面以平移动画来做示例，见下图 4，假设在动画开始时 ChildView 在 ParentView 中的初始位置
			在 (100,200) 处，这时 ParentView 会根据这个坐标来设置 ChildView 的画布，
			在 ParentView 的 dispatchDraw 中它发现 ChildView 有一个平移动画，而且当前的
			平移位置是 (100, 200)，于是它通过调用画布的函数 traslate(100, 200) 来告诉 ChildView 
			在这个位置开始画，这就是动画的第一帧。如果 ParentView 发现 ChildView 有动画，
			就会不断的调用 invalidate() 这个函数，这样就会导致自己会不断的重画，就会不断的调用 
			dispatchDraw 这个函数，这样就产生了动画的后续帧，当再次进入 dispatchDraw 时，
			ParentView 根据平移动画产生出第二帧的平移位置 (500, 200)，然后继续执行上述操作，
			然后产生第三帧，第四帧，直到动画播完。具体算法描述如清单 2：

	以上是以平移动画为例子来说明动画的产生过程，这其中又涉及到两个重要的类型，Animation 和 Transformation
		，这两个类是实现动画的主要的类，Animation 中主要定义了动画的一些属性比如开始时间、持续时间、
		是否重复播放等，这个类主要有两个重要的函数：getTransformation 和 applyTransformation，
		在 getTransformation 中 Animation 会根据动画的属性来产生一系列的差值点，然后将这些差值点传
		给 applyTransformation，这个函数将根据这些点来生成不同的 Transformation，Transformation 
		中包含一个矩阵和 alpha 值，矩阵是用来做平移、旋转和缩放动画的，而 alpha 值是用来做 alpha 动画的
		（简单理解的话，alpha 动画相当于不断变换透明度或颜色来实现动画），以上面的平移矩阵为例子，
		当调用 dispatchDraw 时会调用 getTransformation 来得到当前的 Transformation，这个 Transformation 
		中的矩阵如下：
		所以具体的动画只需要重载 applyTransformation 这个函数即可，类层次图如下：

	}

	6.线程wait(),yelid(),sleep()区别
	{
		1.sleep()     使当前线程(即调用该方法的线程)暂停执行一段时间，让其他线程有机会继续执行，
			但它并不释放对象锁。也就是如果有Synchronized同步块，其他线程仍然不同访问共享数据。
			注意该方法要捕获异常     比如有两个线程同时执行(没有Synchronized)，
			一个线程优先级为MAX_PRIORITY，另一个为MIN_PRIORITY，如果没有Sleep()方法，
			只有高优先级的线程执行完成后，低优先级的线程才能执行；但当高优先级的线程sleep(5000)后，
			低优先级就有机会执行了。     总之，sleep()可以使低优先级的线程得到执行的机会，当然也
			可以让同优先级、高优先级的线程有执行的机会。 
		2.join()     join()方法使调用该方法的线程在此之前执行完毕，也就是等待调用该方法的线程执行
			完毕后再往下继续执行。注意该方法也要捕获异常。 
		3.yield()     它与sleep()类似，只是不能由用户指定暂停多长时间，并且yield()方法只能让同
			优先级的线程有执行的机会。 
		4.wait()和notify()、notifyAll()     这三个方法用于协调多个线程对共享数据的存取，所以必须
			在Synchronized语句块内使用这三个方法。前面说过Synchronized这个关键字用于保护共享数据，
			阻止其他线程对共享数据的存取。但是这样程序的流程就很不灵活了，如何才能在当前线程还
			没退出Synchronized数据块时让其他线程也有机会访问共享数据呢？此时就用这三个方法来灵活控制。    
			wait()方法使当前线程暂停执行并释放对象锁标志，让其他线程可以进入Synchronized数据块，
			当前线程被放入对象等待池中。当调用 notify()方法后，将从对象的等待池中移走一个任意
			的线程并放到锁标志等待池中，只有锁标志等待池中的线程能够获取锁标志；如果锁标志等待
			池中没有线程，则notify()不起作用。     notifyAll()则从对象等待池中移走所有等待那个对象
			的线程并放到锁标志等待池中。     注意 这三个方法都是java.lang.Ojbect的方法! 
		2.run()和start()     这两个方法应该都比较熟悉，把需要并行处理的代码放在run()方法中，
			start()方法启动线程将自动调用 run()方法，这是由Java的内存机制规定的。并且run()方法必
			须是public访问权限，返回值类型为void。
		3.关键字Synchronized     这个关键字用于保护共享数据，当然前提是要分清哪些数据是共享数据。
			每个对象都有一个锁标志，当一个线程访问该对象时，被Synchronized修饰的数据将被“上锁”，
			阻止其他线程访问。当前线程访问完这部分数据后释放锁标志，其他线程就可以访问了。
	}

	6.Java垃圾回收机制
	{
		针对以上特点，我们在使用的时候要注意：

		（1）不要试图去假定垃圾收集发生的时间，这一切都是未知的。比如，方法中的一个临时对象在方法调用完毕后就变成了无用对象，这个时候它的内存就可以被释放。

		（2）Java中提供了一些和垃圾收集打交道的类，而且提供了一种强行执行垃圾收集的方法--调用System.gc()，但这同样是个不确定的方法。Java 中并不保证每次调用该方法就一定能够启动垃圾收集，它只不过会向JVM发出这样一个申请，到底是否真正执行垃圾收集，一切都是个未知数。

		（3）挑选适合自己的垃圾收集器。一般来说，如果系统没有特殊和苛刻的性能要求，可以采用JVM的缺省选项。否则可以考虑使用有针对性的垃圾收集器，比如增量收集器就比较适合实时性要求较高的系统之中。系统具有较高的配置，有比较多的闲置资源，可以考虑使用并行标记/清除收集器。

		（4）关键的也是难把握的问题是内存泄漏。良好的编程习惯和严谨的编程态度永远是最重要的，不要让自己的一个小错误导致内存出现大漏洞。

		（5）尽早释放无用对象的引用。大多数程序员在使用临时变量的时候，都是让引用变量在退出活动域(scope)后，自动设置为null，暗示垃圾收集器来收集该对象，还必须注意该引用的对象是否被监听，如果有，则要去掉监听器，然后再赋空值。

		结束语

		一般来说，Java开发人员可以不重视JVM中堆内存的分配和垃圾处理收集，但是，充分理解Java的这一特性可以让我们更有效地利用资源。同时要注意finalize()方法是Java的缺省机制，有时为确保对象资源的明确释放，可以编写自己的finalize方法。
	}
}
