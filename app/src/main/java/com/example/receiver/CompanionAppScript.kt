package com.example.receiver

object CompanionAppScript {
    const val PYTHON_SCRIPT = """# ====================================================================
#              BLUETOOTH GAMEPAD COMPANION RECEIVER (PC/Mac)
# ====================================================================
# This python script runs on your Windows/Mac computer. It listens to
# low-latency UDP packets transmitted by the app, emulates a real 
# virtual Xbox 360 controller, and displays a floating battery widget.
#
# Requirements:
#   1. Install Python 3 on your computer.
#   2. Install vgamepad library:
#      pip install vgamepad
#   3. Run this script:
#      python companion_receiver.py
#
# Note for macOS: 'vgamepad' supports Windows natively. For macOS,
# standard keyboard/mouse mappings or virtual controller APIs can be used.
# ====================================================================

import socket
import struct
import threading
import tkinter as tk
import sys

# Try to import vgamepad (supports Windows and some Mac environments)
try:
    import vgamepad as vg
    gamepad = vg.VX360Gamepad()
    has_vgamepad = True
    print("[SUCCESS] Virtual Xbox 360 controller initialized.")
except ImportError:
    has_vgamepad = False
    print("[WARNING] 'vgamepad' not installed or not supported. Running in key-mapper mode.")
    print("To enable full Xbox 360 simulation, install: pip install vgamepad")

# Port must match the Port in the Android app settings (default: 5001)
UDP_IP = "0.0.0.0"
UDP_PORT = 5001

# Global state for battery and status
current_battery = 100
app_running = True

# Mapping table from Android buttons to Xbox gamepad buttons
BUTTON_MAPPINGS = {
    0x0001: "A",          # Button A
    0x0002: "B",          # Button B
    0x0004: "X",          # Button X
    0x0008: "Y",          # Button Y
    0x0010: "LB",         // Left Bumper
    0x0020: "RB",         // Right Bumper
    0x0040: "LT",         // Left Trigger (Mapped as button here)
    0x0080: "RT",         // Right Trigger (Mapped as button here)
    0x0100: "BACK",       // Select
    0x0200: "START",      // Start
    0x0400: "LSTICK",     // Left Stick click
    0x0800: "RSTICK",     // Right Stick click
}

# Map virtual buttons in vgamepad
def map_button_to_xbox(button_name, pressed):
    if not has_vgamepad:
        return
    btn_const = None
    if button_name == "A": btn_const = vg.XUSB_BUTTON.XUSB_GAMEPAD_A
    elif button_name == "B": btn_const = vg.XUSB_BUTTON.XUSB_GAMEPAD_B
    elif button_name == "X": btn_const = vg.XUSB_BUTTON.XUSB_GAMEPAD_X
    elif button_name == "Y": btn_const = vg.XUSB_BUTTON.XUSB_GAMEPAD_Y
    elif button_name == "LB": btn_const = vg.XUSB_BUTTON.XUSB_GAMEPAD_LEFT_SHOULDER
    elif button_name == "RB": btn_const = vg.XUSB_BUTTON.XUSB_GAMEPAD_RIGHT_SHOULDER
    elif button_name == "BACK": btn_const = vg.XUSB_BUTTON.XUSB_GAMEPAD_BACK
    elif button_name == "START": btn_const = vg.XUSB_BUTTON.XUSB_GAMEPAD_START
    elif button_name == "LSTICK": btn_const = vg.XUSB_BUTTON.XUSB_GAMEPAD_LEFT_THUMB
    elif button_name == "RSTICK": btn_const = vg.XUSB_BUTTON.XUSB_GAMEPAD_RIGHT_THUMB

    if btn_const:
        if pressed:
            gamepad.press_button(button=btn_const)
        else:
            gamepad.release_button(button=btn_const)

def map_dpad_to_xbox(dpad_mask):
    if not has_vgamepad:
        return
    
    # D-pad bitmask values:
    # 0 = Up, 1 = Down, 2 = Left, 3 = Right, 8 = None
    gamepad.release_button(button=vg.XUSB_BUTTON.XUSB_GAMEPAD_DPAD_UP)
    gamepad.release_button(button=vg.XUSB_BUTTON.XUSB_GAMEPAD_DPAD_DOWN)
    gamepad.release_button(button=vg.XUSB_BUTTON.XUSB_GAMEPAD_DPAD_LEFT)
    gamepad.release_button(button=vg.XUSB_BUTTON.XUSB_GAMEPAD_DPAD_RIGHT)

    if dpad_mask == 0:
        gamepad.press_button(button=vg.XUSB_BUTTON.XUSB_GAMEPAD_DPAD_UP)
    elif dpad_mask == 1:
        gamepad.press_button(button=vg.XUSB_BUTTON.XUSB_GAMEPAD_DPAD_DOWN)
    elif dpad_mask == 2:
        gamepad.press_button(button=vg.XUSB_BUTTON.XUSB_GAMEPAD_DPAD_LEFT)
    elif dpad_mask == 3:
        gamepad.press_button(button=vg.XUSB_BUTTON.XUSB_GAMEPAD_DPAD_RIGHT)
    elif dpad_mask == 4: # Up-Left
        gamepad.press_button(button=vg.XUSB_BUTTON.XUSB_GAMEPAD_DPAD_UP)
        gamepad.press_button(button=vg.XUSB_BUTTON.XUSB_GAMEPAD_DPAD_LEFT)
    elif dpad_mask == 5: # Up-Right
        gamepad.press_button(button=vg.XUSB_BUTTON.XUSB_GAMEPAD_DPAD_UP)
        gamepad.press_button(button=vg.XUSB_BUTTON.XUSB_GAMEPAD_DPAD_RIGHT)
    elif dpad_mask == 6: # Down-Left
        gamepad.press_button(button=vg.XUSB_BUTTON.XUSB_GAMEPAD_DPAD_DOWN)
        gamepad.press_button(button=vg.XUSB_BUTTON.XUSB_GAMEPAD_DPAD_LEFT)
    elif dpad_mask == 7: # Down-Right
        gamepad.press_button(button=vg.XUSB_BUTTON.XUSB_GAMEPAD_DPAD_DOWN)
        gamepad.press_button(button=vg.XUSB_BUTTON.XUSB_GAMEPAD_DPAD_RIGHT)

def udp_listener_thread(battery_label_update_callback):
    global current_battery, app_running
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    
    try:
        sock.bind((UDP_IP, UDP_PORT))
        print(f"[INFO] Listening on UDP {UDP_IP}:{UDP_PORT} for low-latency commands...")
    except Exception as e:
        print(f"[ERROR] Failed to bind to port {UDP_PORT}: {e}")
        return

    while app_running:
        try:
            sock.settimeout(1.0)
            data, addr = sock.recvfrom(1024)
            if len(data) < 24:
                continue

            # Parse 24 bytes header (GPAD, 4 floats, 1 short, 1 byte, 1 byte)
            header = data[0:4].decode('ascii', errors='ignore')
            if header != "GPAD":
                continue

            lx, ly, rx, ry, buttons_mask, dpad_mask, battery_percent = struct.unpack('<ffffHBB', data[4:24])

            # Update battery widget
            if battery_percent != current_battery:
                current_battery = battery_percent
                battery_label_update_callback(battery_percent)

            # Apply stick mappings to virtual Xbox Controller
            if has_vgamepad:
                # vgamepad expects floats/shorts between -32768 and 32767 for joysticks
                gamepad.left_joystick_float(x_value_float=lx, y_value_float=-ly) # Invert Y for standard Xbox axes
                gamepad.right_joystick_float(x_value_float=rx, y_value_float=-ry)

                # LT and RT triggers
                trigger_l = 1.0 if (buttons_mask & 0x0040) != 0 else 0.0
                trigger_r = 1.0 if (buttons_mask & 0x0080) != 0 else 0.0
                gamepad.left_trigger_float(value_float=trigger_l)
                gamepad.right_trigger_float(value_float=trigger_r)

                # Map standard buttons
                for mask, name in BUTTON_MAPPINGS.items():
                    # LT/RT handled separately as analog triggers, but map standard buttons
                    if name not in ["LT", "RT"]:
                        is_pressed = (buttons_mask & mask) != 0
                        map_button_to_xbox(name, is_pressed)

                # Map D-pad
                map_dpad_to_xbox(dpad_mask)

                # Send report to system
                gamepad.update()

        except socket.timeout:
            continue
        except Exception as e:
            print(f"[ERROR] Connection processing error: {e}")

    sock.close()

# Tkinter floating desktop battery and status widget
def run_gui():
    global app_running
    root = tk.Tk()
    root.title("Gamepad Widget")
    
    # Stay on top, no window decorations (borderless)
    root.attributes("-topmost", True)
    root.overrideredirect(True)
    
    # Widget screen placement (e.g. top right corner)
    screen_width = root.winfo_screenwidth()
    root.geometry(f"160x70+{screen_width - 180}+40")
    
    # Styling
    root.configure(bg="#1E1E2E")
    
    # Window dragging handlers
    def start_move(event):
        root.x = event.x
        root.y = event.y

    def stop_move(event):
        root.x = None
        root.y = None

    def on_move(event):
        deltax = event.x - root.x
        deltay = event.y - root.y
        x = root.winfo_x() + deltax
        y = root.winfo_y() + deltay
        root.geometry(f"+{x}+{y}")

    root.bind("<ButtonPress-1>", start_move)
    root.bind("<ButtonRelease-1>", stop_move)
    root.bind("<B1-Motion>", on_move)

    title_label = tk.Label(root, text="🕹️ PHONE GAMEPAD", font=("Consolas", 9, "bold"), fg="#89B4FA", bg="#1E1E2E")
    title_label.pack(pady=4)

    battery_var = tk.StringVar(value="🔋 Battery: 100%")
    battery_label = tk.Label(root, textvariable=battery_var, font=("Consolas", 10), fg="#A6E3A1", bg="#1E1E2E")
    battery_label.pack()

    status_label = tk.Label(root, text="● Connected (Low Latency)", font=("Consolas", 8), fg="#F38BA8", bg="#1E1E2E")
    status_label.pack(pady=2)

    def update_battery_label(percent):
        battery_var.set(f"🔋 Battery: {percent}%")
        # Color based on battery level
        if percent < 20:
            battery_label.config(fg="#F38BA8")
        elif percent < 50:
            battery_label.config(fg="#F9E2AF")
        else:
            battery_label.config(fg="#A6E3A1")

    # Exit mechanism
    def on_close():
        global app_running
        app_running = False
        root.destroy()
        sys.exit(0)

    # Simple close button (X) on top-right of widget
    close_btn = tk.Button(root, text="×", font=("Consolas", 10, "bold"), fg="#FFFFFF", bg="#1E1E2E", bd=0, activebackground="#F38BA8", command=on_close)
    close_btn.place(x=140, y=2, width=16, height=16)

    # Start UDP listener thread
    listener = threading.Thread(target=udp_listener_thread, args=(lambda p: root.after(0, update_battery_label, p),), daemon=True)
    listener.start()

    root.mainloop()

if __name__ == "__main__":
    run_gui()
"""
}
