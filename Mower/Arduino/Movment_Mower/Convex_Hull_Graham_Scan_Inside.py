# Python3 program for the above approach

# Function To Check Clockwise
# Orientation
def cw(a, b, c):
	p = a[0] * (b[1] - c[1]) + b[0] * (c[1] - a[1]) + c[0] * (a[1] - b[1])

	return p < 0

# Function To Check Counter
# Clockwise Orientation
def ccw(a, b, c):
	p = a[0] * (b[1] - c[1]) + b[0] * (c[1] - a[1]) + c[0] * (a[1] - b[1])

	return p > 0

# Graham Scan algorithm to find Convex
# Hull from given points
def convexHull(v):

	# Sort the points
	v.sort()
	

	n = len(v)
	if (n <= 3):
		return v

	# Set starting and ending points as
	# left bottom and top right
	p1 = v[0]
	p2 = v[n - 1]

	# Vector to store points in
	# upper half and lower half
	up = []
	down = []

	# Insert StartingEnding Points
	up.append(tuple(p1))
	down.append(p1)

	# Iterate over points
	for i in range(1, n):
		
		if i == n - 1 or (not ccw(p1, v[i], p2)):

			while len(up) > 1 and ccw(up[len(up) - 2], up[len(up) - 1], v[i]):

				# Exclude this point
				# if we can form better

				up.pop()
			
			up.append(tuple(v[i]))
		

		if i == n - 1 or (not cw(p1, v[i], p2)):

			while (len(down) > 1) and cw(down[len(down) - 2], down[len(down) - 1], v[i]):

				# Exclude this point
				# if we can form better
				down.pop()
			
			down.append(v[i])
		
	# Combine upper and lower half
	for i in range(len(down) - 2, -1, -1):
		up.append(tuple(down[i]))

	# Remove duplicate points
	up = set(up)
	up = list(up)

	# Return the points on Convex Hull
	return up

# Function to find if point lies inside
# a convex polygon
def isInside( points, query):

	# Include the query point in the
	# polygon points
	points.append(query)

	# Form a convex hull from the points
	points = convexHull(points)

	# Iterate over the points
	# of convex hull
	for x in points:

		# If the query point lies
		# on the convex hull
		# then it wasn't inside
		if x == query:
			return False
	
	# Otherwise it was Inside
	return True

# Driver Code

# Points of the polygon
# given in any order
n = 7
points = [[1, 1 ], [2, 1 ], [ 3, 1 ], [ 4, 1 ], [ 4, 2 ], [4, 3 ], [ 4, 4 ]]

# Query Points
query = [ 3, 2 ]

# Check if its inside
if (isInside(points, query)) :
	print("YES")
	
else :
	print("NO")
	
# This code is contributed by phasing17.
