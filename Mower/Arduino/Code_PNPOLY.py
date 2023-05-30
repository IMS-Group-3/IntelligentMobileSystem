#main code from link below 
#https://wrfranklin.org/Research/Short_Notes/pnpoly.html
#translated to pyton by Oskar Rundberg

def pnpoly( nvert:int, vertx:list, verty:list, testx:int, testy:int):
    j = nvert - 1 
    c = False
    for i in range(nvert): 
        if ( ((verty[i]>testy) != (verty[j]>testy)) and
         (testx < (vertx[j]-vertx[i]) * (testy-verty[i]) / (verty[j]-verty[i]) + vertx[i]) ):
            c = not c
        j = i
    return c


def isInside(points:list[list[int,int]], querry:list[int]):
    xlist = []
    ylist = []

    for i in range(len(points)):
        xlist.append(points[i][0])
        ylist.append(points[i][1])

    print(xlist)
    print(ylist)

    return pnpoly(len(points),xlist,ylist,querry[0],querry[1])

def manualInput():
    xcord = []
    ycord = []
    while(True):
        inputString = input("Please Enter a Cordinate in form x,y \nRegret last entry enter u, when done enter d if: ")
        if inputString == 'd':
            break
        elif inputString == 'u':
            if len(xcord) > 0:
                xcord.pop()
                ycord.pop()
            else:
                print("List Empty")
        else:
            x,y = inputString.split(",") 
            xcord.append(int(x))
            ycord.append(int(y))

    inputString = input("Please Enter Cordinate of point to check in form x,y: ")
    x,y = inputString.split(",")

    x = int(x)
    y = int(y)

    if len(ycord) == len(xcord):
        n = len(ycord)
        print(pnpoly(n,xcord,ycord,x,y))
    else:
        if len(ycord) > len(xcord):
            print("Error Cordinate list y longer then x")
        else:
            print("Error Cordinate list x longer then y")


print("Start")
while True:
    match (input("Enter 1 to manuly enter cordinates. 2 to read from a list. q to Quit: ")):
        case "1":
            manualInput()
        case "2":
            polygon = [[1,1],[7,1],[10,4],[7,6],[1,6],[1,1],[0,0],[2,2],[7,2],[7,5],[2,5],[2,2],[0,0]]
            print(polygon)
            querry = []
            x,y = input("Please Enter Cordinate of point to check in form x,y: ").split(",")
            querry.append(int(x))
            querry.append(int(y))
            print(querry)
            print(isInside(polygon,querry))
        case "q":
            break
        case _:
            print("Invalid Comand please try again")
print("End")