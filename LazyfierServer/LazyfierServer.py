from socket import socket, AF_INET, SOCK_DGRAM, SOL_SOCKET, SO_BROADCAST, gethostname
from os import path, system
from subprocess import Popen, CalledProcessError, PIPE, check_output
import sys
from time import sleep, time
from tkinter import Tk, Frame, LEFT, BOTTOM, Label, Button, PhotoImage, X
import threading
from tendo import singleton


configFileName = "config.txt"
BROADCAST_PORT = 3014

host = ""
IP = 0
port = 0
password = ""
count = 15
root_password = ""
screen_width_middle = 0
screen_height_middle = 0

MSG_ACK = "ACK\n"
MSG_UNKNOWN = "Unknown command\n"
MSG_BADPASS = "Bad password\n"

flag_shutdown_init = False
msg_from_client = []

if 'linux' in sys.platform:
    OS_PLATFORM = 'linux'
    S = '/'
else:
    OS_PLATFORM = 'windows'
    S = '\\'

if OS_PLATFORM == 'linux':
    PLAY = "XF86AudioPlay"
    PAUSE = "XF86AudioPause"
    STOP = "XF86AudioStop"
    NEXT = "XF86AudioNext"
    PREV = "XF86AudioPrev"
    SHUT_DOWN = "XF86Close"
    VOL_DOWN = "XF86AudioLowerVolume"
    VOL_UP = "XF86AudioRaiseVolume"
    FORWARD = "XF86Forward"
    REWIND = "XF86Rewind"
    CLOSE = "alt+F4"
    SPACE = "space"


    def send_key(key):
        run_async_process(["xdotool", "key", key])


    def get_active_window_name():
        try:
            result = Popen(["xdotool", "getwindowfocus", "getwindowname"], stdout=PIPE).communicate()[0]
            return result.decode()
        except CalledProcessError as e:
            return ""


    def set_cursor(x_value, y_value):
        global screen_width_middle
        global screen_height_middle

        x_coord = (screen_width_middle * x_value) // 32768
        y_coord = (screen_height_middle * y_value) // 32768

        system("xdotool mousemove " + str(x_coord) + " " + str(y_coord))


    def send_mouse(command):
        if command.startswith("MOUSE_LEFT_DOWN"):
            system("xdotool mousedown 1")
        elif command.startswith("MOUSE_LEFT_UP"):
            system("xdotool mouseup 1")
        elif command.startswith("MOUSE_RIGHT_DOWN"):
            system("xdotool mousedown 3")
        elif command.startswith("MOUSE_RIGHT_UP"):
            system("xdotool mouseup 3")
        elif command.startswith("MOUSE_SCROLL_DOWN"):
            system("xdotool click 4")
        elif command.startswith("MOUSE_SCROLL_UP"):
            system("xdotool click 5")


    def get_screen_resolution():
        global screen_width_middle
        global screen_height_middle

        xr = [s for s in check_output("xrandr").decode("utf-8").split() if "+0+" in s]
        scr = [int(n) / 2 for n in xr[0].split("+")[0].split("x")]

        screen_width_middle = scr[0]
        screen_height_middle = scr[1]

        
else:
    from win32api import keybd_event, mouse_event, SetCursorPos
    from winsound import PlaySound, SND_FILENAME
    from win32gui import GetWindowText, GetForegroundWindow
    from win32con import MOUSEEVENTF_LEFTDOWN, MOUSEEVENTF_LEFTUP, MOUSEEVENTF_RIGHTDOWN, MOUSEEVENTF_RIGHTUP, \
                         MOUSEEVENTF_WHEEL, MOUSEEVENTF_MOVE, MOUSEEVENTF_ABSOLUTE, KEYEVENTF_KEYUP

    winKeyCodes = {'shift': 0x10,
                   'ctrl': 0x11,
                   'alt': 0x12,
                   'pause': 0x13,
                   'spacebar': 0x20,
                   'page_up': 0x21,
                   'page_down': 0x22,
                   'end': 0x23,
                   'home': 0x24,
                   'left_arrow': 0x25,
                   'up_arrow': 0x26,
                   'right_arrow': 0x27,
                   'down_arrow': 0x28,
                   '0': 0x30,
                   '9': 0x39,
                   'a': 0x41,
                   'f': 0x46,
                   'j': 0x4A,
                   'k': 0x4B,
                   'l': 0x4C,
                   'x': 0x58,
                   'F4': 0x73,
                   'browser_back': 0xA6,
                   'browser_forward': 0xA7,
                   'browser_refresh': 0xA8,
                   'browser_stop': 0xA9,
                   'browser_search': 0xAA,
                   'browser_favorites': 0xAB,
                   'browser_start_and_home': 0xAC,
                   'volume_mute': 0xAD,
                   'volume_down': 0xAE,
                   'volume_up': 0xAF,
                   'next_track': 0xB0,
                   'previous_track': 0xB1,
                   'stop_media': 0xB2,
                   'play/pause_media': 0xB3,
                   'play_key': 0xFA
                   }

    PLAY = "play/pause_media"
    STOP = "stop_media"
    NEXT = "next_track"
    PREV = "previous_track"
    VOL_DOWN = "volume_down"
    VOL_UP = "volume_up"
    SPACE = "spacebar"
    FORWARD = 'browser_forward'
    REWIND = 'browser_back'


    def send_key(key, modifier=''):
        global winKeyCodes

        key_virtual_value = winKeyCodes[key]

        if modifier != '':
            keybd_event(winKeyCodes[modifier], 0, 0, 0)
        keybd_event(key_virtual_value, 0, 0, 0)
        sleep(0.05)
        keybd_event(key_virtual_value, 0, KEYEVENTF_KEYUP, 0)

        if modifier != '':
            keybd_event(winKeyCodes[modifier], 0, KEYEVENTF_KEYUP, 0)


    def get_active_window_name():
        return GetWindowText(GetForegroundWindow())


    def set_cursor(x_value, y_value):

        # SetCursorPos((1000, 20))
        mouse_event(MOUSEEVENTF_MOVE | MOUSEEVENTF_ABSOLUTE, x_value, y_value, 0, 0)


    def send_mouse(command):
        if command.startswith("MOUSE_LEFT_DOWN"):
            mouse_event(MOUSEEVENTF_LEFTDOWN, 0, 0, 0, 0)
        elif command.startswith("MOUSE_LEFT_UP"):
            mouse_event(MOUSEEVENTF_LEFTUP, 0, 0, 0, 0)
        elif command.startswith("MOUSE_RIGHT_DOWN"):
            mouse_event(MOUSEEVENTF_RIGHTDOWN, 0, 0, 0, 0)
        elif command.startswith("MOUSE_RIGHT_UP"):
            mouse_event(MOUSEEVENTF_RIGHTUP, 0, 0, 0, 0)
        elif command.startswith("MOUSE_SCROLL_DOWN"):
            mouse_event(MOUSEEVENTF_WHEEL, 0, 0, -50, 0)
        elif command.startswith("MOUSE_SCROLL_UP"):
            mouse_event(MOUSEEVENTF_WHEEL, 0, 0, 50, 0)


me = singleton.SingleInstance() # will sys.exit(-1) if other instance is running

# determine if application is a script file or frozen exe
if getattr(sys, 'frozen', False):
    current_path = path.dirname(sys.executable)
elif __file__:
    current_path = path.dirname(path.realpath(__file__))


class InfoBox(Tk):
    global OS_PLATFORM

    def __init__(self, box_title, box_text, icon_type='info'):
        Tk.__init__(self)

        if OS_PLATFORM == 'windows':
            self.iconbitmap(current_path + '\\launcher.ico')
        else:
            self.image_icon = PhotoImage(file=current_path + "/ic_launcher.png")
            self.wm_iconphoto(True, self.image_icon)

        self.title(box_title)
        self.wm_title(box_title)
        self.minsize(300, 50)

        self.frame1 = Frame(self)
        self.frame1.pack(pady=0, padx=0, fill=X)

        if icon_type == "chat":
            icon_path = current_path + S + 'icon_chat.gif'
        else:
            icon_path = current_path + S + 'icon_info.gif'

        self.image = PhotoImage(file=icon_path)
        Label(self.frame1, image=self.image).pack(side=LEFT, padx=10, pady=5)

        if icon_type == "chat":
            Label(self.frame1, text=box_text, justify=LEFT, font=("Courier", 30, "bold")).pack(padx=5)
        else:
            Label(self.frame1, text=box_text, justify=LEFT).pack(padx=5)

        Button(self.frame1, text="Dismiss", command=self.dismiss, width=10).pack(side=BOTTOM, padx=5, pady=5)

    def dismiss(self, event=None):
        self.wm_withdraw()
        self.destroy()


class CountDownBox(Tk):
    global flag_shutdown_init
    global OS_PLATFORM

    def __init__(self):
        Tk.__init__(self)

        self.count = 15

        self.title_string = 'Shutdown in %d s' % self.count
        self.title(self.title_string)
        self.wm_title(self.title_string)
        self.minsize(300, 50)

        if OS_PLATFORM == 'windows':
            self.iconbitmap(current_path + '\\launcher.ico')
        else:
            self.image_icon = PhotoImage(file=current_path + "/ic_launcher.png")
            self.wm_iconphoto(True, self.image_icon)

        self.frame1 = Frame(self)
        self.frame1.pack(pady=0, padx=0, fill=X)

        icon_path = current_path + S + 'icon_shutdown.gif'
        self.image = PhotoImage(file=icon_path)
        Label(self.frame1, image=self.image).pack(side=LEFT, padx=10, pady=5)

        Label(self.frame1, text="Lazyfier server initiated system shutdown.", justify=LEFT).pack(padx=5)

        self.count_down_label = Label(self.frame1, text="", justify=LEFT)
        self.count_down_label.pack()

        self.update_clock()

    def update_clock(self):

        if flag_shutdown_init:

            self.count_down_label.configure(text="Seconds left: %d \n close to stop!" % self.count)
            self.after(1000, self.update_clock)
            self.count -= 1

            self.title_string = 'Shutdown in %d s' % self.count
            self.title(self.title_string)
            self.wm_title(self.title_string)

            if self.count == 0:
                if OS_PLATFORM == 'linux':
                    system("shutdown -h now")
                else:
                    system("shutdown.exe -f -s -t 0")
                self.destroy()

        else:
            self.destroy()


def run_async_process(command_list):
    try:
        Popen(command_list)
    except Exception as e:
        print("Lazyfier Server Error:", e)
        pass


def msg_countdown_box():
    box = CountDownBox()
    box.after(100, lambda: box.focus_force())
    box.mainloop()


def msg_info_box(msg_title, msg_text):
    box = InfoBox(msg_title, msg_text)
    box.mainloop()


def get_ip():

    IPaddr = ""
    Socket = socket(AF_INET, SOCK_DGRAM)
    try:
        # doesn't even have to be reachable
        Socket.connect(('10.255.255.255', 0))
        IPaddr = Socket.getsockname()[0]
    except:
        Socket.close()
    finally:
        Socket.close()

    if len(IPaddr.split('.')) != 4:
        return None

    return IPaddr


def find_available_port(IPAddr):
    # list of valid ports. You are free to choose, but these are also defined in the android app
    ports = [2000, 3000, 4000, 5000]
    address = IPAddr

    for portIdx in range(0, len(ports)):
        portNum = ports[portIdx]
        Socket = socket()

        try:
            Socket.bind((address, portNum))
            Socket.listen(2)
            Socket.close()

            return portNum
        except:
            Socket.close()
            pass

    if portNum == 0:
        msg_info_box("Lazyfier Server Error!", "No available port")
        sys.exit()

    return 0


def setup():

    detected_host = gethostname()

    # write config file
    config_file = open(current_path + S + 'config.txt', 'w')
    config_file.write("# Please set you own password to protect access from other Lazyfier devices.\n")
    config_file.write("# It can be any combination of characters which you also configure in the Android app.\n")
    config_file.write("# The default name is the machine name, but you may change it, ")
    config_file.write("# it is how it is identified by the Android app.\n\n")
    config_file.write("\nhostPassword=")
    config_file.write("\nhostName=" + detected_host)

    config_file.close()

    # display detected settings
    finish_msg = "It appears that the server has been started for the first time,\nso it initiated setup.\n"
    finish_msg += "Server name: " + detected_host + "\nPassword:"
    finish_msg += '\n\nIf you wish to change these settings, you may do so by editing "config.txt" '

    msg_info_box("Lazyfier settings:", finish_msg)


# read config
if not path.exists(current_path + S + configFileName):
    setup()

    # check again
    if not path.exists(current_path + S + configFileName):
        msg_info_box("Lazyfier Server Error!", "Failed to setup server for the first time.")
        sys.exit()


# reading the contents of the config file
f = open(current_path + S + configFileName, 'r')
content = f.read().splitlines()
f.close()

for lineIdx in range(0, len(content)):
    if content[lineIdx].startswith('#'):
        continue

    line = content[lineIdx].split('=')
    if line[0] == "hostName":
        if len(line) > 1:
            host = line[1].split('#')[0].strip()
        else:
            host = gethostname()

    if line[0] == "hostPassword":
        if len(line) > 1:
            password = line[1].split('#')[0].strip()
        else:
            password = ""


# sleep(10)
IP = get_ip()

while IP is None:
    sleep(10)
    IP = get_ip()

port = find_available_port(IP)


def thread_beacon():
    global IP
    global BROADCAST_PORT
    global OS_PLATFORM

    beacon_msg = "NAME:" + gethostname() + ":"

    broadcaster = socket(AF_INET, SOCK_DGRAM)
    broadcaster.setsockopt(SOL_SOCKET, SO_BROADCAST, 1)
    broadcaster.bind((get_ip(), 0))

    while True:

        # restart server if IP changed
        if get_ip() != IP:
            if OS_PLATFORM == 'windows':
                run_async_process([current_path + "\\restart_server.bat"])
            else:
                run_async_process([current_path + '/lazyfier-restart'])

        broadcaster.sendto(beacon_msg.encode(), ('<broadcast>', BROADCAST_PORT))

        sleep(1)


# start_beacon_service
t_beacon = threading.Thread(target=thread_beacon)
t_beacon.start()

get_screen_resolution()


def thread_comms_loop():
    global IP
    global port
    global msg_from_client
    global flag_shutdown_init
    global OS_PLATFORM

    last_shutdown_timestamp = 0

    my_socket = socket()
    my_socket.bind((IP, port))
    my_socket.listen(2)

    while True:  # keep listening until shutdown        
        try:
            conn, addr = my_socket.accept()            

            while True:
                data = conn.recv(50).decode()

                if not data:
                    break

                if data == "NAME":
                    data = "NAME:" + host + '\n'
                    conn.send(data.encode())
                else:
                    if password != "":
                        client_msg = data.split(' ')
                        client_pass = client_msg[0]
                        if len(client_msg) > 1:
                            client_command = client_msg[(len(client_pass) + 1):]
                        else:
                            client_command = ""
                    else:
                        client_pass = ""
                        client_command = data

                    if client_pass == password:

                        if client_command.startswith("PLAY"):
                            conn.send(MSG_ACK.encode())
                            if "youtube" in get_active_window_name().lower():
                                send_key("k")
                            else:
                                send_key(PLAY)

                        elif client_command.startswith("SPACE"):
                            conn.send(MSG_ACK.encode())
                            send_key(SPACE)

                        elif client_command.startswith("STOP"):
                            conn.send(MSG_ACK.encode())
                            send_key(STOP)

                        elif client_command.startswith("FULLSCR"):
                            conn.send(MSG_ACK.encode())
                            send_key("f")

                        elif client_command.startswith("NEXT"):
                            conn.send(MSG_ACK.encode())
                            send_key(NEXT)

                        elif client_command.startswith("FORWARD"):
                            conn.send(MSG_ACK.encode())
                            if "youtube" in get_active_window_name().lower():
                                send_key("l")
                            elif "vlc media player" in get_active_window_name().lower():
                                if OS_PLATFORM == 'linux':
                                    send_key("alt+Right")
                                else:
                                    send_key("right_arrow", 'alt')
                            else:
                                send_key(FORWARD)

                        elif client_command.startswith("PREV"):
                            conn.send(MSG_ACK.encode())
                            send_key(PREV)

                        elif client_command.startswith("REWIND"):
                            conn.send(MSG_ACK.encode())
                            if "youtube" in get_active_window_name().lower():
                                send_key("j")
                            elif "vlc media player" in get_active_window_name().lower():
                                if OS_PLATFORM == 'linux':
                                    send_key("alt+Left")
                                else:
                                    send_key("left_arrow", 'alt')
                            else:
                                send_key(REWIND)

                        elif client_command.startswith("VOL_DOWN"):
                            conn.send(MSG_ACK.encode())
                            send_key(VOL_DOWN)

                        elif client_command.startswith("VOL_UP"):
                            conn.send(MSG_ACK.encode())
                            send_key(VOL_UP)

                        elif client_command.startswith("CLOSE"):
                            conn.send(MSG_ACK.encode())
                            if OS_PLATFORM == 'linux':
                                send_key(CLOSE)
                            else:
                                send_key('F4', 'alt')

                        elif client_command.startswith("MSG:"):
                            msg_from_client.append(client_command[4:])
                            conn.send(MSG_ACK.encode())

                        elif client_command.startswith("CURSOR:"):
                            conn.send(MSG_ACK.encode())
                            coordinates = client_command.split(":")
                            if len(coordinates) > 2:
                                set_cursor(int(coordinates[1], 10), int(coordinates[2], 10))

                        elif client_command.startswith("MOUSE"):
                            conn.send(MSG_ACK.encode())
                            send_mouse(client_command)

                        elif client_command.startswith("ACTIVE_WINDOW"):
                            response = "TITLE:" + get_active_window_name() + "\n"
                            conn.send(response.encode())

                        elif client_command == "SHUT_DOWN":
                            conn.send(MSG_ACK.encode())

                            if flag_shutdown_init:
                                if time() - last_shutdown_timestamp > 1:
                                    last_shutdown_timestamp = 0
                                    flag_shutdown_init = False
                                    if OS_PLATFORM == 'linux':
                                        run_async_process(['play', current_path + '/startup.wav'])
                                    else:
                                        PlaySound(current_path + "\\startup.wav", SND_FILENAME)
                            else:
                                last_shutdown_timestamp = time()
                                flag_shutdown_init = True
                                if OS_PLATFORM == 'linux':
                                    run_async_process(['play', current_path + '/theend.wav'])
                                else:
                                    PlaySound(current_path + "\\theend.wav", SND_FILENAME)

                        else:
                            conn.send(MSG_UNKNOWN.encode())
                            print("Unknown command: ", client_command)

                    else:
                        conn.send(MSG_BADPASS.encode())

            conn.close()            

        except:
            pass


t_comms = threading.Thread(target=thread_comms_loop)
t_comms.start()

while True:

    num_of_messages = len(msg_from_client)
    if num_of_messages > 0:
        message = msg_from_client[0]
        del msg_from_client[0]
        msg_info_box("Message from Lazyfier remote:", message)

    if flag_shutdown_init:
        msg_countdown_box()
