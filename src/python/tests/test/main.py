import copy
NOT_AN_EDGE = -1

def is_edge(graph,row,col):
	return graph[row][col] != NOT_AN_EDGE

def move_ok(graph,row,col):
	return (
		row < len(graph) and
		row >= 0 and
		col < len(graph) and
		col >= 0 and
		is_edge(graph,row,col)
	)

def get_move(graph,row,col):
	return [(row,col)] if move_ok(graph,row,col) else []

def get_moves(graph,row,col):
	moves = []
	moves += get_move(graph,row+1,col)
	moves += get_move(graph,row-1,col)
	moves += get_move(graph,row,col+1)
	moves += get_move(graph,row,col-1)
	return moves

def move_legal(path,move,visited=None):
	return move not in path and move not in visited if visited is not None else True

def has_legal_move(graph,path,node):
	moves = get_moves(graph,*node)
	for move in moves:
		if move_legal(path,move):
			return True
	return False

def cost(graph,row,col):
	return graph[row][col]

def is_goal(start,goal):
	return start == goal

def backtrack(graph,path):
	node = path.pop()
	while not has_legal_move(graph,path,node): node = path.pop()
	return path

def cost_first(graph,start,goal):
	visited = []
	current = start
	path = []
	while not is_goal(current,goal):
		path.append(current)
		moves = get_moves(graph,*current)
		best_move = None
		for move in moves:
			if is_goal(move,goal):
				best_move = move
				break
			if move_legal(path,move,visited):
				if best_move is None or cost(graph,*move) <= cost(graph,*best_move):
					best_move = move
		if best_move is None:
			visited.append(current)
			path = backtrack(graph,path)
			current = path.pop() if len(path) > 0 else start
		else:
			current = best_move
		if len(visited) > 5:
			break
	path.append(current)
	return path

graph = [
	[1,1,1,1,1],
	[1,1,1,1,1],
	[1,1,1,1,1]
]
print cost_first(graph,(0,0),(0,3))