import React, { useState } from "react";
import "intro.js/introjs.css";
import { Steps } from "intro.js-react";
import { tourOptions, tourSteps } from "./Tutorial";
import {BsQuestionCircle} from "react-icons/bs"
import { useRecoilState } from "recoil";
import isActiveFloatingState from "../../../recoil/isActiveFloatingState";
import tw from "twin.macro";

const TutorialButton: React.FC = () => {
    const [stepsEnabled, setStepsEnabled] = useState(false);
    const [initialStep] = useState(0);
    const [tour] = useState({
        options: tourOptions,
        steps: tourSteps,
      });
    const onExit = () => {
        setStepsEnabled(false);
      };
    const handleHelp = () => {
        setStepsEnabled((prev) => !prev);
        setIsActiveFloating(true)
      };
    const ICON = BsQuestionCircle
    const [, setIsActiveFloating] = useRecoilState(isActiveFloatingState)

    return(
        <div>
            <Steps
                enabled={stepsEnabled}
                steps={tour.steps}
                initialStep={initialStep}
                onExit={onExit}
                options={tour.options}
            />
        <ICON css={tw`absolute text-black pl-3 pt-3`} onClick={handleHelp}></ICON>
        </div>
    )
}

export default TutorialButton;