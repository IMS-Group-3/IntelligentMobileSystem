#main code from link below
#https://wrfranklin.org/Research/Short_Notes/pnpoly.html
#translated to pyton by Oskar Rundberg


class IMS_Polygon:
    standard_points = [[0, 0], [0, 900], [900, 900], [900, 0], [0, 0]]

    @staticmethod
    def pnpoly(nvert: int, vertx: list, verty: list, testx: int, testy: int):
        j = nvert - 1
        c = False
        for i in range(nvert):
            if (((verty[i] > testy) != (verty[j] > testy))
                    and (testx < (vertx[j] - vertx[i]) * (testy - verty[i]) /
                         (verty[j] - verty[i]) + vertx[i])):
                c = not c
            j = i
        return c

    def isInside(self, boundary_field: list[list[int, int]],
                 current_coordinate: list[int]):
        xlist = []
        ylist = []

        for i in range(len(boundary_field)):
            xlist.append(boundary_field[i][0])
            ylist.append(boundary_field[i][1])
        # Print lists of coordinates for debugging
        #print(xlist)
        #print(ylist)

        return self.pnpoly(len(boundary_field), xlist, ylist,
                           current_coordinate[0], current_coordinate[1])

    ## Use this if we want to create our own polygon (Not finished)
    def manualInput():
        xcord = []
        ycord = []
        while (True):
            inputString = input(
                "Please Enter a Cordinate in form x,y \nRegret last entry enter u, when done enter d if: "
            )
            if inputString == 'd':
                break
            elif inputString == 'u':
                if len(xcord) > 0:
                    xcord.pop()
                    ycord.pop()
                else:
                    print("List Empty")
            else:
                x, y = inputString.split(",")
                xcord.append(int(x))
                ycord.append(int(y))

        inputString = input(
            "Please Enter Cordinate of point to check in form x,y: ")
        x, y = inputString.split(",")

        x = int(x)
        y = int(y)

        if len(ycord) == len(xcord):
            n = len(ycord)
            print(pnpoly(n, xcord, ycord, x, y))
        else:
            if len(ycord) > len(xcord):
                print("Error Cordinate list y longer then x")
            else:
                print("Error Cordinate list x longer then y")