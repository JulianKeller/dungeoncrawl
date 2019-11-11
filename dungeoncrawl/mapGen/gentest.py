from random import randint
from random import *
import timeit
import sys

sys.setrecursionlimit(5000)

FLOOR = 0
WALL = 1

"""
This script generates a random map tile using a system
of randomly generated rooms and hallways. Rooms are open areas with
multiple exit points. Hallways are long, thin rooms which only contain
one exit on either end.

Generation can be somewhat customized using various flags.

Extensibility
-Some kind of flood-fill search is necessary for determining which cells 
 the player can reach for spawning items/enemies
-If fill values are changed to 1s and 0s, each level could be compressed
 using bitmap compression
"""

def print2Array( array ):
	for i in array:
		for j in i:
			print(j, end="")
		print()
	print('\n\n')


def printResultArray( array ):
	print('[')
	for i in array:
		print(f'{i},')
	print(']\n')

def saveResult(array, count):
	with open(f'maps/map{count}.txt', 'w') as fp:
		for row in array:
			for i in row:
				fp.write(f'{str(i)} ')
			fp.write('\n')


def makeRoom(y, x, w, h, level):
	#make a room with the given dimensions
	touched = [] #list of cells touched in making this room
	for row in range(y-(h//2), y+(h//2)+1): #+1 to include the last
		for col in range(x-(w//2), x+(w//2)+1):
			if row >= len(level)-1 or col >= len(level[row])-1 or row < 1 or col < 1:
				#went off the screen, exit
				return (level, touched)
			level[row][col] = FLOOR
			touched.append((row, col))
	level[y][x]= FLOOR
	return (level, touched)


def generateRoomsAndHallways(maxx, maxy):
	#generate a room or a hallway from each adjacent cell

	#generation flags
	"""
	Room chance and hall chance affect how often a room or hallway will be generated.
	A higher room chance leads to more open areas (good for outdoor maps and boss fights),
	while a higher hall chance leads to more mazelike terrain.
	Good values for a dungeon level are 10% room chance and 50% hall chance (there
	must also be some chance that no room or hall will spawn).
	"""
	#general tile initialization steps
	"""
	maxx and maxy determine the size of the grid
	"""
	# maxx = 100
	# maxy = 50



	roomChance = 20
	hallChance = 50

	"""
	maxHallLen is the maximum length of a hallway. If a hallway is chosen to generate,
	a random length less than this will be chosen.
	"""
	maxHallLen = 8
	hallWidth = 2

	"""
	If a room is chosen to generate, then random dimensions less than these values will
	be chosen. Bigger rooms are suitable for open/outdoor areas.
	"""
	maxRoomW = 4
	maxRoomH = 4

	"""
	Each room (not hall) will have a random number of exits less than this value.
	Too many exits can lead to levels with very sparse walls.
	"""
	maxRoomExits = 4

	"""
	The maximum number of cells which may be expanded.
	A larger value will increase runtime and create more open areas as more cells
	are expanded.
	"""
	iterations = 3000

	level = []
	#fill level with walls to start
	for y in range(0, maxy):
		row = []
		for x in range(0, maxx):
			# row.append("█")
			row.append(WALL)
			# row.append('██')
		level.append(row)

	#get a random starting point
	# rx = randint(0, maxx)
	# ry = randint(0, maxy)
	rx = maxx//2
	ry = maxy//2

	open = [(ry, rx)]
	closed = []

	while len(open) > 0 and iterations > 0:
		iterations = iterations - 1

		#pop off the top node and find its adjacencies
		current = open.pop(0)

		adj = [(current[0], current[1]+1), (current[0], current[1]-1),
		(current[0]+1, current[1]), (current[0]-1, current[1])]

		#add to closed list
		closed.append(current)

		for cell in adj:
			if cell in closed:
				continue
			if cell[0] > 1 and cell[0] < maxy-1 and cell[1] > 1 and cell[1] < maxx-1:
				open.insert(0, cell)
				#pick hallway or room
				r = randint(0, 100)
				if r < roomChance:
					#make a room with random bounded dimensions
					h = randint(0, maxRoomH)
					w = randint(0, maxRoomW)
					room = makeRoom(cell[0], cell[1], w, h, level)
					level = room[0]
					numExits = randint(1, maxRoomExits)
					e = 0
					for thing in room[1]:
						if e < numExits:
							r = randint(0, 100)
							if r <= 20:
								#make an exit
								open.insert(0, thing)
								e = e + 1
							else:
								closed.append(thing)
						else:
							closed.append(thing)
				else:
					#make a hallway with random bounded length
					#in a random direction
					dir = choice(["n", "e", "s", "w"])

					#get a random hallway length
					hallLen = randint(0, maxHallLen)
					ny = 0
					nx = 0
					h = 0
					w = 0
					if dir == "n":
						#hallway pointing north
						ny = y-(hallLen//2)
						nx = x
						h = hallLen
						w = hallWidth
					elif dir == "e":
						#hallway pointing east
						ny = y
						nx = x + (hallLen//2)
						h = hallWidth
						w = hallLen
					elif dir == "s":
						#hallway pointing south
						ny = y + (hallLen//2)
						nx = x
						h = hallLen
						w = hallWidth
					elif dir == "w":
						#hallway pointing west
						ny = y
						nx = x - (hallLen//2)
						h = hallWidth
						w = hallLen

					room = makeRoom(ny, nx, w, h, level)
					level = room[0]

					for thing in room[1]:
						closed.append(thing)
	# print2Array(level)
	# print()
	# printResultArray(level)
	return level




if __name__ == '__main__':
	start = timeit.default_timer()
	for i in range(1000):
		lvl = generateRoomsAndHallways(80, 48)
		saveResult(lvl, i)
	print(f'Run Time: {timeit.default_timer() - start:.2} seconds')






