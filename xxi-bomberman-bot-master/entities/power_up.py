class PowerUp:
    def __init__(self, type, location):
        if type == 'Domain.Entities.PowerUps.SuperPowerUp, Domain':
            self.type = 'super'
        elif type == 'Domain.Entities.PowerUps.BombRaduisPowerUpEntity, Domain':
            self.type = 'radius'
        else:
            self.type = 'bag'

        self.location = location

    def isSuper(self):
        return self.type == 'super'

    def isRadius(self):
        return self.type == 'radius'

    def isBag(self):
        return self.type == 'bag'
