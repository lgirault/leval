function leval
	echo (count $argv) arguments
	if test (count $argv) -gt 3
		echo "to many arguments"
		echo "leval <client|server> [server host name]"
		return
	end


    ## set debug -Dakka.loglevel=DEBUG -Dakka.actor.debug.receive=on

    ## set serverHostName -Dleval.server.hostname=52.59.65.1
    ## set serverHostName -Dleval.server.hostname=88.191.174.144
    set serverHostName -Dleval.server.hostname=127.0.0.1
    ## set serverPort -Dleval.server.port=2552

    if test $argv[1] = "client"
        set module "client"
    ##    set host -Dakka.remote.netty.tcp.hostname=88.191.174.144
    ##    set bhost -Dakka.remote.netty.tcp.bind-hostname=192.168.0.4

    ##    set host -Dakka.remote.netty.tcp.hostname=127.0.0.1
    ##    set bhost -Dakka.remote.netty.tcp.bind-hostname=127.0.0.1

    ##    set port -Dakka.remote.netty.tcp.port=$argv[2]
    ##    set bport -Dakka.remote.netty.tcp.bind-port=$argv[2]

        set main leval.GUIClient
    else if test $argv[1] = "creator"
        set module "client"
        set main leval.QuickCreatorClient
    else if test $argv[1] = "joiner"
        set module "client"
        set main leval.QuickJoiningClient
    else
        set host -Dakka.remote.netty.tcp.hostname=127.0.0.1
        set bhost -Dakka.remote.netty.tcp.bind-hostname=127.0.0.1
#        set host -Dakka.remote.netty.tcp.hostname=88.191.174.144
#        set bhost -Dakka.remote.netty.tcp.bind-hostname=192.168.0.4
        set module "server"
	    set main leval.Server
    end

    source /home/lorilan/projects/leval/$module/target/CLASSPATH

    java -cp $CLASSPATH $debug $host $bhost $port $bport $serverHostName $serverPort $main

end
