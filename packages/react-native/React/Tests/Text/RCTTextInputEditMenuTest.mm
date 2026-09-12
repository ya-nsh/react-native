/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

#import <React/RCTTextInputUtils.h>
#import <React/RCTUITextField.h>
#import <React/RCTUITextView.h>
#import <XCTest/XCTest.h>

#if !TARGET_OS_TV

using namespace facebook::react;

@interface RCTTextInputEditMenuTest : XCTestCase
@end

@implementation RCTTextInputEditMenuTest {
  UIWindow *_window;
  UIView<RCTBackedTextInputViewProtocol> *_input;
  NSMutableArray<NSDictionary *> *_events;
  BOOL _currentMenu;
}

- (void)setUp
{
  [super setUp];
  _events = [NSMutableArray new];
  _currentMenu = YES;
  _window = [[UIWindow alloc] initWithFrame:CGRectMake(0, 0, 320, 480)];
  _window.rootViewController = [UIViewController new];
  [_window makeKeyAndVisible];
  [self installInput:[RCTUITextField new]];
}

- (void)tearDown
{
  [_input resignFirstResponder];
  _window.hidden = YES;
  _input = nil;
  _window = nil;
  [super tearDown];
}

- (void)installInput:(UIView<RCTBackedTextInputViewProtocol> *)input
{
  [_input removeFromSuperview];
  _input = input;
  _input.frame = CGRectMake(0, 0, 200, 80);
  [_window.rootViewController.view addSubview:_input];
  _input.attributedText = [[NSAttributedString alloc] initWithString:@"A😀B"];
  [_input becomeFirstResponder];
  [self selectRange:NSMakeRange(1, 2)];
}

- (void)selectRange:(NSRange)range
{
  UITextPosition *start = [_input positionFromPosition:_input.beginningOfDocument offset:range.location];
  UITextPosition *end = [_input positionFromPosition:start offset:range.length];
  [_input setSelectedTextRange:[_input textRangeFromPosition:start toPosition:end] notifyDelegate:NO];
}

- (UIMenu *)menuForRange:(NSRange)range suggestedActions:(NSArray<UIMenuElement *> *)suggestedActions
{
  return RCTCreateTextInputEditMenu(
      _input,
      range,
      suggestedActions,
      {{"quote", "Quote"}},
      ^BOOL {
        return self->_currentMenu;
      },
      ^(NSString *actionId, NSString *text, NSRange selection) {
        [self->_events addObject:@{
          @"id" : actionId,
          @"text" : text,
          @"start" : @(selection.location),
          @"end" : @(NSMaxRange(selection)),
        }];
      });
}

- (void)activateCustomAction:(UIMenu *)menu
{
  UIMenu *group = (UIMenu *)menu.children.lastObject;
  UIAction *action = (UIAction *)group.children.firstObject;
  // Exercise the public UIAction handler without relying on private UIKit selectors.
  UIControl *control = [UIControl new];
  [control addAction:action forControlEvents:UIControlEventTouchUpInside];
  [control sendActionsForControlEvents:UIControlEventTouchUpInside];
}

- (void)testPreservesSystemMenuAndAddsInlineActions
{
  UIAction *copy = [UIAction actionWithTitle:@"Copy"
                                       image:nil
                                  identifier:nil
                                     handler:^(__unused UIAction *action){
                                     }];
  UIMenu *menu = [self menuForRange:NSMakeRange(1, 2) suggestedActions:@[ copy ]];
  XCTAssertEqualObjects(menu.children.firstObject, copy);
  UIMenu *group = (UIMenu *)menu.children.lastObject;
  XCTAssertEqual(group.options, UIMenuOptionsDisplayInline);
  XCTAssertEqualObjects(group.children.firstObject.title, @"Quote");
}

- (void)testDeliversUTF16SelectionForSingleAndMultilineInputs
{
  for (UIView<RCTBackedTextInputViewProtocol> *input in @[ [RCTUITextField new], [RCTUITextView new] ]) {
    [self installInput:input];
    [self activateCustomAction:[self menuForRange:NSMakeRange(1, 2) suggestedActions:@[]]];
    XCTAssertEqualObjects(_events.lastObject, (@{@"id" : @"quote", @"text" : @"A😀B", @"start" : @1, @"end" : @3}));
    XCTAssertEqualObjects(_input.attributedText.string, @"A😀B");
  }
  XCTAssertEqual(_events.count, 2);
}

- (void)testRejectsEmptyOutOfBoundsAndOverflowingRanges
{
  XCTAssertNil([self menuForRange:NSMakeRange(0, 0) suggestedActions:@[]]);
  XCTAssertNil([self menuForRange:NSMakeRange(5, 1) suggestedActions:@[]]);
  XCTAssertNil([self menuForRange:NSMakeRange(1, NSUIntegerMax) suggestedActions:@[]]);
  XCTAssertNil([self menuForRange:NSMakeRange(NSNotFound, 1) suggestedActions:@[]]);
}

- (void)testSuppressesCustomActionsForHiddenMenusAndSecureInputs
{
  _input.contextMenuHidden = YES;
  XCTAssertNil([self menuForRange:NSMakeRange(1, 2) suggestedActions:@[]]);
  _input.contextMenuHidden = NO;
  _input.secureTextEntry = YES;
  XCTAssertNil([self menuForRange:NSMakeRange(1, 2) suggestedActions:@[]]);
}

- (void)testIgnoresActionAfterTextOrSelectionChanges
{
  UIMenu *menu = [self menuForRange:NSMakeRange(1, 2) suggestedActions:@[]];
  _input.attributedText = [[NSAttributedString alloc] initWithString:@"AXXB"];
  [self selectRange:NSMakeRange(1, 2)];
  [self activateCustomAction:menu];
  XCTAssertEqual(_events.count, 0);

  menu = [self menuForRange:NSMakeRange(1, 2) suggestedActions:@[]];
  [self selectRange:NSMakeRange(0, 1)];
  [self activateCustomAction:menu];
  XCTAssertEqual(_events.count, 0);
}

- (void)testIgnoresInvalidatedMenuAndDetachedInput
{
  UIMenu *menu = [self menuForRange:NSMakeRange(1, 2) suggestedActions:@[]];
  _currentMenu = NO;
  [self activateCustomAction:menu];
  XCTAssertEqual(_events.count, 0);

  _currentMenu = YES;
  [_input removeFromSuperview];
  [self activateCustomAction:menu];
  XCTAssertEqual(_events.count, 0);
}

- (void)testRechecksSecureEntryWhenActionIsActivated
{
  UIMenu *menu = [self menuForRange:NSMakeRange(1, 2) suggestedActions:@[]];
  _input.secureTextEntry = YES;
  [self activateCustomAction:menu];
  XCTAssertEqual(_events.count, 0);
}

- (void)testAnEmptyItemListPreservesTheDefaultMenu
{
  XCTAssertNil(RCTCreateTextInputEditMenu(
      _input,
      NSMakeRange(1, 2),
      @ [],
      {},
      ^BOOL {
        return YES;
      },
      ^(__unused NSString *actionId, __unused NSString *text, __unused NSRange selection) {
        XCTFail(@"Empty menus must not dispatch actions");
      }));
}

@end
#endif
