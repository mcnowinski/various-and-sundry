# importing only those functions
# which are needed
from tkinter import *
import tkinter.filedialog
import tkinter.messagebox
import tkinter.ttk as tkttk
from time import strftime
import numpy as np
import os
import csv

data = []


def insertData():
    for row in tree.get_children():
        tree.delete(row)
    sortedData = sorted(data)
    i = 1
    for j in sortedData:
        tree.insert("", i, text=j[0], values=j[1:])
        i += 1


def getTime(rawInput):
    splitTime = rawInput.split()
    return splitTime[1]


def getDuration(start, end):
    startSplit = start.split(":")
    endSplit = end.split(":")
    startMinute = int(startSplit[0])*60 + int(startSplit[1])
    endMinute = int(endSplit[0])*60 + int(endSplit[1])
    return (endMinute - startMinute)


def openCsv():
    People = {}
    filename = tkinter.filedialog.askopenfilename(initialdir="/", title="Select File to Open",
                                                  filetypes=(("CSV", "*.csv"),))
    if filename == '':
        return

    try:
        file = open(filename, 'r', encoding="utf-8")
        reader = csv.reader(file)
        next(reader)

        for row in reader:
            name = row[0].upper()
            email = row[1]
            startTime = getTime(row[2])
            endTime = getTime(row[3])
            duration = getDuration(startTime, endTime)

            if name in People:
                People[name] = (
                    People[name][0],
                    People[name][1],
                    People[name][2],
                    People[name][3] + 1,
                    People[name][4] + duration
                )
            else:
                People[name] = (
                    name,
                    email,
                    startTime,
                    1,
                    duration
                )

        data.clear()
        for key, val in People.items():
            data.append([key] + list(val))

        insertData()
    except:
        tkinter.messagebox.showinfo(
            "Error", "Ensure input file is formatted correctly.")


def exportCsv():
    saveDir = tkinter.filedialog.asksaveasfilename(initialdir="/", title="Select File to Export",
                                                   defaultextension='.csv', filetypes=(("CSV", "*.csv"),))
    if saveDir == '':
        return

    try:
        saveData = np.array(sorted(data))
        with open(saveDir, "w", newline="", encoding='utf-8') as output:
            writer = csv.writer(output)
            writer.writerow(
                ["Name", "Email", "Join Time", "Duration", "Sessions"])
            writer.writerows(
                saveData[:, [1, 2, 3, 5, 4]])
    except:
        tkinter.messagebox.showinfo(
            "Error", "Ensure output file can be written.\nIf it is open in Excel, please close it, then retry.")


def about():
    tkinter.messagebox.showinfo(
        "About", "Zoom Attendance v1.0\nby The Drs. Manhattan")


def treeview_sort_column(tv, col, reverse, is_number):
    l = [(tv.set(k, col), k) for k in tv.get_children('')]
    if is_number:
        l.sort(key=lambda t: float(t[0]), reverse=reverse)
    else:
        l.sort(key=lambda t: t[0], reverse=reverse)

    # rearrange items in sorted positions
    for index, (val, k) in enumerate(l):
        tv.move(k, '', index)

    # reverse sort next time
    tv.heading(col, command=lambda _col=col: treeview_sort_column(
        tv, _col, not reverse, is_number))


# the gui
# creating tkinter window
root = Tk()
root.title('Zoom Attendance')
root.geometry('800x600')

# Creating Menubar
menubar = Menu(root)

# Adding File Menu and commands
file = Menu(menubar, tearoff=0)
menubar.add_cascade(label='File', menu=file)
file.add_command(label='Open', command=openCsv)
file.add_command(label='Export', command=exportCsv)
file.add_separator()
file.add_command(label='Exit', command=root.destroy)

# Adding Help Menu
help_ = Menu(menubar, tearoff=0)
menubar.add_cascade(label='Help', menu=help_)
help_.add_command(label='About', command=about)

frame = tkinter.Frame(root)
frame.place(relheight=0.9, relwidth=0.9, relx=0.05, rely=0.05)

tree = tkttk.Treeview(frame, height=500)
tree['show'] = 'headings'

vsb = tkttk.Scrollbar(orient="vertical", command=tree.yview)
vsb.pack(side='right', fill='y')
tree.configure(yscrollcommand=vsb.set)

tree["columns"] = ("Name", "Email", "Join Time", "Sessions", "Duration")

tree.column("#0", width=100)
tree.column("Name", width=100)
tree.column("Email", width=100)
tree.column("Duration", width=30)
tree.column("Join Time", width=10)
tree.column("Sessions", width=10)

tree.heading("#0", text="#0", anchor=tkinter.W)
tree.heading("Name", text="Name", anchor=tkinter.W,
             command=lambda: treeview_sort_column(tree, "Name", False, False))
tree.heading("Email", text="Email", anchor=tkinter.W,
             command=lambda: treeview_sort_column(tree, "Email", False, False))
tree.heading("Join Time", text="Join Time", anchor=tkinter.W,
             command=lambda: treeview_sort_column(tree, "Join Time", False, False))
tree.heading("Sessions", text="# Sessions", anchor=tkinter.W,
             command=lambda: treeview_sort_column(tree, "Sessions", False, True))
tree.heading("Duration", text="Total Duration (min)", anchor=tkinter.W,
             command=lambda: treeview_sort_column(tree, "Duration", False, True))

tree.pack(side=tkinter.TOP, fill=tkinter.X)

# display Menu
root.config(menu=menubar)
root.resizable(False, False)
mainloop()
