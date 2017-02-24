class Wall:
    def __init__(self, type, location):
        if type == 'Domain.Entities.IndestructibleWallEntity, Domain':
            self.destructible = False
        else:
            self.destructible = True

        self.location = location
