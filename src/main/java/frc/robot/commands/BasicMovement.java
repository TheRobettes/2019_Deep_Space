/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.commands;

import edu.wpi.first.wpilibj.command.Command;
import frc.robot.subsystems.BasicController;

public class BasicMovement extends Command {
  private BasicController targetSubsystem;
  private double speed;

  public BasicMovement(BasicController targetSubsystem, double speed) {
    this.targetSubsystem = targetSubsystem;
    this.speed = speed;
    requires(targetSubsystem);
    // Use requires() here to declare subsystem dependencies
    // eg. requires(chassis);
  }

  // Called just before this Command runs the first time
  @Override
  protected void initialize() {
    targetSubsystem.activate();
  }

  // Called repeatedly when this Command is scheduled to run
  @Override
  protected void execute() {
    targetSubsystem.move(speed);
  }

  // Make this return true when this Command no longer needs to run execute()
  @Override
  protected boolean isFinished() {
    return false;
  }

  // Called once after isFinished returns true
  @Override
  protected void end() {
    targetSubsystem.move(0);
    targetSubsystem.deactivate();
  }

  // Called when another command which requires one or more of the same
  // subsystems is scheduled to run
  @Override
  protected void interrupted() {
    end();
  }
}
