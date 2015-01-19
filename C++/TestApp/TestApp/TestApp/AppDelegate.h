//
//  AppDelegate.h
//  TestApp
//
//  Created by Terry Skotz on 5/6/14.
//  Copyright (c) 2014 Terry Skotz. All rights reserved.
//

#import <Cocoa/Cocoa.h>

@interface AppDelegate : NSObject <NSApplicationDelegate>

@property (assign) IBOutlet NSWindow *window;

@property (readonly, strong, nonatomic) NSPersistentStoreCoordinator *persistentStoreCoordinator;
@property (readonly, strong, nonatomic) NSManagedObjectModel *managedObjectModel;
@property (readonly, strong, nonatomic) NSManagedObjectContext *managedObjectContext;

- (IBAction)saveAction:(id)sender;
- (IBAction)mute:(id)sender;
- (IBAction)takeFloatValueForVolumeFrom:(id)sender;

@end
